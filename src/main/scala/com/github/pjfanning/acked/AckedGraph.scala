package com.github.pjfanning.acked

import org.apache.pekko.stream._
import scala.annotation.unchecked.uncheckedVariance

trait AckedShape { self =>
  type Self <: AckedShape
  type PekkoShape <: org.apache.pekko.stream.Shape
  val pekkoShape: PekkoShape
  def wrapShape(pekkoShape: PekkoShape): Self
}

trait AckedGraph[+S <: AckedShape, +M] {
  type Shape = S @uncheckedVariance
  protected [acked] val shape: Shape
  type PekkoShape = shape.PekkoShape
  val pekkoGraph: Graph[PekkoShape, M]
  def wrapShape(pekkoShape: pekkoGraph.Shape): shape.Self =
    shape.wrapShape(pekkoShape)

  def withAttributes(attr: Attributes): AckedGraph[S, M]

  def named(name: String): AckedGraph[S, M] = withAttributes(Attributes.name(name))

  def addAttributes(attr: Attributes): AckedGraph[S, M]
}

final class AckedSourceShape[+T](s: SourceShape[AckTup[T]]) extends AckedShape {
  type Self = AckedSourceShape[T] @uncheckedVariance
  type PekkoShape = SourceShape[AckTup[T]] @uncheckedVariance
  val pekkoShape = s
  def wrapShape(pekkoShape: PekkoShape @uncheckedVariance): Self =
    new AckedSourceShape(pekkoShape)
}

final class AckedSinkShape[-T](s: SinkShape[AckTup[T]]) extends AckedShape {
  type Self = AckedSinkShape[T] @uncheckedVariance
  type PekkoShape = SinkShape[AckTup[T]] @uncheckedVariance
  val pekkoShape = s
  def wrapShape(pekkoShape: PekkoShape @uncheckedVariance): Self =
    new AckedSinkShape(pekkoShape)
}

class AckedFlowShape[-I, +O](s: FlowShape[AckTup[I], AckTup[O]]) extends AckedShape {
  type Self = AckedFlowShape[I, O] @uncheckedVariance
  type PekkoShape = FlowShape[AckTup[I], AckTup[O]] @uncheckedVariance
  val pekkoShape = s
  def wrapShape(pekkoShape: PekkoShape @uncheckedVariance): Self =
    new AckedFlowShape(pekkoShape)
}
