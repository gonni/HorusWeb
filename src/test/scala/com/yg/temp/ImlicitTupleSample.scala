package com.yg.temp

//object ImlicitTupleSample {
//
//  class MyTable extends AbstractTable[(Int, String)] with TupleShapeImplicits {
//
//    def id = column[Int]("NO")
//    def name = column[String]("NAME")
//
//    override def * : ProvenShape[(Int, String)] = ProvenShape.provenShapeOf(id, name)
//  }
//
//  def main(args: Array[String]): Unit = {
//    println("Active ..")
//    type Schema = (Int, String)
//
////    val a : ProvenShape[Schema] = ProvenShape.testShapeOf((Rep[Int]))
//  }
//}
//
//trait TupleShapeImplicits {
//  @inline
//  implicit final def tuple2Shape[Level <: ShapeLevel, M1,M2, U1,U2, P1,P2](implicit u1: Shape[_ <: Level, M1, U1, P1], u2: Shape[_ <: Level, M2, U2, P2]): Shape[Level, (M1,M2), (U1,U2), (P1,P2)] =
//    new TupleShape[Level, (M1,M2), (U1,U2), (P1,P2)](u1,u2)
//}
//
//final class TupleShape[Level <: ShapeLevel, M <: Product, U <: Product, P <: Product](val shapes: Shape[_ <: ShapeLevel, _, _, _]*) {
//
//}
//
//trait ShapeLevel
//trait FlatShapeLevel extends ShapeLevel
//trait Shape [Level <: ShapeLevel, -Mixed_, Unpacked_, Packed_]
//trait TestShape[T, U]
//trait ProvenShape[U] {
////  val shape: Shape[_ <: FlatShapeLevel, _, U, _]
//}
//object ProvenShape {
//  implicit def provenShapeOf[T, U](v: T) (implicit sh: Shape[_ <: FlatShapeLevel, T, U, _]): ProvenShape[U] =
//    new ProvenShape[U] {
////      val shape: Shape[_ <: FlatShapeLevel, _, U, _] = sh
//    }
//  implicit def testShapeOf[T, U](v: T) (implicit sh: TestShape[T, U]): ProvenShape[U] =
//    new ProvenShape[U] {
//
//    }
//}
//
//trait TypedType[T]
//trait Rep[C]
//abstract class AbstractTable[U] {
//  def column[C](n: String): Rep[C] = {
//    ???
//  }
//  def * : ProvenShape[U]
//}