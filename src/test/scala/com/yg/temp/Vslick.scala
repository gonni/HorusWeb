package com.yg.temp

object Vslick {
  def main(args: Array[String]): Unit = {
//    import ImplicitEngine.provenShapeOf

    println("Active System")

//    val provenShape : ProvenShape[(Int, String)] = ProvenShape.provenShapeOf(Rep.create[Int], Rep.create[String])

  }
}

object ImplicitEngine {
//  implicit def provenShapeOf[T, U](v: T)(implicit sh: Shape[_ <: FlatShapeLevel, T, U, _]) : ProvenShape[U] =
//    new ProvenShape[U] {
//      val shape = sh
//    }
}

trait ImplicitTupleShape {
//  @inline
//  implicit final def tuple2Shape[Level <: ShapeLevel, M1,M2, U1,U2, P1,P2]
//  (implicit u1: Shape[_ <: Level, M1, U1, P1], u2: Shape[_ <: Level, M2, U2, P2])
//    : Shape[Level, (M1,M2), (U1,U2), (P1,P2)] =
//    new TupleShape[Level, (M1,M2), (U1,U2), (P1,P2)](u1,u2)
//
//  @inline
//  implicit final def tuple4Shape[Level <: ShapeLevel, M1,M2,M3,M4, U1,U2,U3,U4, P1,P2,P3,P4]
//  (implicit u1: Shape[_ <: Level, M1, U1, P1], u2: Shape[_ <: Level, M2, U2, P2], u3: Shape[_ <: Level, M3, U3, P3], u4: Shape[_ <: Level, M4, U4, P4]): Shape[Level, (M1,M2,M3,M4), (U1,U2,U3,U4), (P1,P2,P3,P4)] =
//    new TupleShape[Level, (M1,M2,M3,M4), (U1,U2,U3,U4), (P1,P2,P3,P4)](u1,u2,u3,u4)
}

//class TupleShape[Level <: ShapeLevel, M <: Product, U <: Product, P <: Product]
//  (val shapes: Shape[_ <: ShapeLevel, _, _, _]*) extends Shape[Level, M, U, P]
//   {
//
//}

//trait Rep[T]
//object Rep {
//  def create[T] = new Rep[T] {}
//}
//trait ProvenShape[U]
//abstract class Shape[Level <: ShapeLevel, -Mixed_, Unpacked_, Packed_] {}
//
//trait ShapeLevel
//trait Product extends Any with Equals
//
//final class TupleShape[Level <: ShapeLevel, M <: Product, U <: Product, P <: Product](val shapes: Shape[_ <: ShapeLevel, _, _, _]*) {}