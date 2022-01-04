package com.yg.vs

object Virtual extends TupleShapeImplicits with RepShapeImplicits {
  val types = new JdbcTypes
  implicit def intCon = types.intJdbcType
  implicit def stringCon = types.stringJdbcType
  //  implicit def proven = ProvenShape.

  class Hell extends Table[(Int, String)]("TB_HELL") {
    def id = column[Int]("HELL_ID")
    def firstName = column[String]("FIRST_NAME")
    //    def lastName = column[String]("LAST_NAME")
    //    def status = column[String]("STATUS")

    def *
    = ProvenShape.proveShapeOf(id, firstName)

    //    def * = ProvenShape.proveShapeOf(id, firstName)(tuple2Shape(Shape.repColumnShape, Shape.repColumnShape))
    //    def * = (id, firstName)
  }

  def main(args: Array[String]): Unit = {

  }
}

trait ProvenShape[U] {
  def value: Any
}

object ProvenShape {
  implicit def proveShapeOf[T, U](v: T)(implicit sh: Shape[_ <: FlatShapeLevel , T, U, _]): ProvenShape[U] =
    new ProvenShape[U] {
      override def value: Any = ???
    }
}

//trait ShapeLevel
//abstract class Shape[L<:ShapeLevel,A,B,C]
abstract class Shape[Level <: ShapeLevel, -Mixed_, Unpacked_, Packed_] {

}
object Shape extends TupleShapeImplicits with RepShapeImplicits {

}

class TupleShape[Level <: ShapeLevel, A, B, C](val shapes: Shape[_ <: ShapeLevel, _, _, _]*) extends Shape[Level, A, B, C] {

}

trait BaseTypedType[T]
trait RepShapeImplicits {
  @inline implicit def repColumnShape[T : BaseTypedType, Level <: ShapeLevel] = RepShape[Level, Rep[T], T]
}

object RepShape extends Shape[FlatShapeLevel, Rep[_], Any, Rep[_]] {
  def apply[Level <: ShapeLevel, MP <: Rep[_], U]: Shape[Level, MP, U, MP] = this.asInstanceOf[Shape[Level, MP, U, MP]]
}

trait ShapeLevel
trait FlatShapeLevel extends ShapeLevel
//object RepShape extends Shape[FlatShapeLevel, Rep[_], Any, Rep[_]] {
//
//}

trait TupleShapeImplicits {
  @inline
  implicit final def tuple2Shape[Level <: ShapeLevel, M1,M2, U1,U2, P1,P2](implicit u1: Shape[_ <: Level, M1, U1, P1], u2: Shape[_ <: Level, M2, U2, P2]): Shape[Level, (M1,M2), (U1,U2), (P1,P2)] =
    new TupleShape[Level, (M1,M2), (U1,U2), (P1,P2)](u1,u2)
  @inline
  implicit final def tuple4Shape[Level <: ShapeLevel, M1, M2, M3, M4, U1, U2, U3, U4, P1, P2, P3, P4](implicit u1: Shape[_ <: Level, M1, U1, P1], u2: Shape[_ <: Level, M2, U2, P2], u3: Shape[_ <: Level, M3, U3, P3], u4: Shape[_ <: Level, M4, U4, P4]): Shape[Level, (M1, M2, M3, M4), (U1, U2, U3, U4), (P1, P2, P3, P4)] =
    new TupleShape[Level, (M1, M2, M3, M4), (U1, U2, U3, U4), (P1, P2, P3, P4)](u1, u2, u3, u4)
}

trait Rep[T]
abstract class AbstractTable[T](val tableName: String) extends Rep[T] {
  type TableElementType = T
  def * : ProvenShape[T]
}
abstract class Table[T](tableName: String) extends AbstractTable[T](tableName) {
  def column[C](n: String) : Rep[C] = new Rep[C] {

  }
}