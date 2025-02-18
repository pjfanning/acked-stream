package com.github.pjfanning.acked

import org.apache.pekko.pattern.ask
import org.apache.pekko.stream.ActorMaterializer
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

class AckedSinkSpec extends AnyFunSpec with Matchers with ActorSystemTest {

  describe("head") {
    it("acknowledges only the first element") {
      case class LeException(msg: String) extends Exception(msg)
      val input = (Stream.continually(Promise[Unit]) zip Range.inclusive(1, 5)).toList
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      Try(await(AckedSource(input).runWith(AckedSink.head)))
      input.map { case (p, _) =>
        p.tryFailure(LeException("didn't complete"))
        Try(await(p.future)) match {
          case Success(_) => None
          case Failure(LeException(msg)) => Some(msg)
          case Failure(e) => throw e
        }
      } should be (Seq(None, Some("didn't complete"), Some("didn't complete"), Some("didn't complete"), Some("didn't complete")))
    }
  }
}
