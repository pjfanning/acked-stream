package com.github.pjfanning.acked

import org.apache.pekko.NotUsed
import org.apache.pekko.stream._
import org.apache.pekko.stream.scaladsl._
import org.apache.pekko.stream.stage._

import scala.concurrent._
import scala.collection.mutable.{Buffer, LinkedHashMap}

object Components {

  /**
    Request bundling buffer.

    Borrowed heavily from Akka-stream 2.0-M1 implementation. Works like a normal
    buffer; however, duplicate items in the buffer get bundled, rather than
    queued; when the item into which the duplicate item was bundled gets acked,
    the duplicate item (and all other cohort bundled items) are acked.

    FIFO, except when duplicate items are bundled into items later in the queue.

    In order for bundling to work, items MUST be comparable by value (IE case
    classes) and MUST be immutable (IE case classes that don't use var).
    Ultimately, the input item is used as a key in a hashmap.

    @param size The size of the buffer. Bundled items do not count against the
    size.
    @param overflowStrategy How should we handle buffer overflow? Note: items
    are failed with DroppedException.

    @return An AckedFlow which runs the bundling buffer component.
  **/
  def bundlingBuffer[T](
    size: Int,
    overflowStrategy: OverflowStrategy
  ): AckedFlow[T, T, NotUsed] =
    AckedFlow {
      Flow[(Promise[Unit], T)].via(BundlingBuffer(size, overflowStrategy))
    }

  sealed abstract class BundlingBufferException(msg: String)
      extends RuntimeException(msg)
  case class BufferOverflowException(msg: String)
      extends BundlingBufferException(msg)
  case class DroppedException(msg: String) extends BundlingBufferException(msg)

  /* we have to pull these out again and make the capitals for
   * pattern matching. Akka is the ultimate hider of useful
   * types. */
  private val DropHead = OverflowStrategy.dropHead
  private val DropTail = OverflowStrategy.dropTail
  private val DropBuffer = OverflowStrategy.dropBuffer
  private val DropNew = OverflowStrategy.dropNew
  private val Backpressure = OverflowStrategy.backpressure
  private val Fail = OverflowStrategy.fail

  case class BundlingBuffer[U](size: Int, overflowStrategy: OverflowStrategy)
      extends GraphStage[FlowShape[(Promise[Unit], U), (Promise[Unit], U)]] {
    type T = (Promise[Unit], U)

    val in = Inlet[T]("BundlingBuffer.in")
    val out = Outlet[T]("BundlingBuffer.out")

    override def shape: FlowShape[T, T] = FlowShape.of(in, out)
    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {
        private val promises: LinkedHashMap[U, Promise[Unit]] =
          LinkedHashMap.empty
        private val buffer: Buffer[U] = Buffer.empty
        private def bufferIsFull: Boolean = buffer.length >= size

        private var isHoldingUpstream = false
        private var isHoldingDownstream = false

        private def dequeue(): T = {
          val v = buffer.remove(0)
          (promises.remove(v).get, v)
        }

        private def enqueue(v: T): Unit = {
          promises.get(v._2) match {
            case Some(p) =>
              v._1.completeWith(p.future)
            case None =>
              promises(v._2) = v._1
              buffer.append(v._2)
          }
        }

        private def dropped(values: U*): Unit =
          values.foreach { i =>
            promises
              .remove(i)
              .map(
                _.tryFailure(
                  DroppedException(
                    s"message was dropped due to buffer overflow; size = $size"
                  )
                )
              )
          }

        private def emitAll(): Unit = {
          val vs = buffer.toSeq.map { v =>
            (promises.remove(v).get, v)
          }
          if (vs.nonEmpty) emitMultiple(out, vs.iterator)
          buffer.clear()
        }

        private def grabAndPull() = {
          if (isAvailable(in)) enqueue(grab(in))
          if (!hasBeenPulled(in)) pull(in)
        }

        private val inHandler: InHandler =
          new InHandler {
            override def onUpstreamFinish(): Unit = {
              emitAll()
              completeStage()
            }

            override def onPush(): Unit = {
              if (bufferIsFull) {
                overflowStrategy match {
                  case DropHead =>
                    dropped(buffer.remove(0))
                    grabAndPull()
                  case DropTail =>
                    dropped(buffer.remove(buffer.length - 1))
                    grabAndPull()
                  case DropBuffer =>
                    dropped(buffer.toSeq: _*)
                    buffer.clear()
                    grabAndPull()
                  case DropNew =>
                    grab(in)._1.tryFailure(
                      DroppedException(
                        s"message was dropped due to buffer overflow; size = $size"
                      )
                    )
                    if (!hasBeenPulled(in)) pull(in)
                  case Fail =>
                    dropped(buffer.toSeq: _*)
                    buffer.clear()
                    failStage(
                      new BufferOverflowException(
                        s"Buffer overflow (max capacity was: $size)!"
                      )
                    )
                  case Backpressure =>
                    isHoldingUpstream = true
                }
              } else grabAndPull()
              if (isHoldingDownstream && isAvailable(out) && buffer.nonEmpty) {
                push(out, dequeue())
                isHoldingUpstream = false
                isHoldingDownstream = false
              }
            }

          }

        private val outHandler: OutHandler =
          new OutHandler {
            override def onPull(): Unit = {
              if (isClosed(in)) completeStage()
              else if (buffer.isEmpty) isHoldingDownstream = true
              else {
                push(out, dequeue())
                if (isHoldingUpstream) {
                  isHoldingUpstream = false
                  pull(in)
                }
              }
            }
          }

        override def preStart(): Unit = pull(in)

        setHandler(in, inHandler)
        setHandler(out, outHandler)
      }
  }
}
