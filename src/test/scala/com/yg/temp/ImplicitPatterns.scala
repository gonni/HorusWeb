package com.yg.temp

import java.io.File

object ImplicitPatterns {
  // value type converter
  def widen[T, V](x: T)(implicit widener: Widener[T, V]): V = widener.widen(x)
  class Widener[T, V](val widen: T => V)
  object Widener{
    implicit object FloatWiden extends Widener[Float, Double](_.toDouble)
    implicit object ByteWiden extends Widener[Byte, Short](_.toShort)
    implicit object ShortWiden extends Widener[Short, Int](_.toInt)
    implicit object IntWiden extends Widener[Int, Long](_.toLong)
  }

  // class
  def extend[T, V, R](tuple: T, value: V)(implicit extender: Extender[T, V, R]): R = {
    extender.extend(tuple, value)
  }
  case class Extender[T, V, R](val extend: (T, V) => R)
  object Extender{
    implicit def tuple2[T1, T2, V, R] = Extender[(T1, T2), V, (T1, T2, V)]{
      case ((t1, t2), v) => (t1, t2, v)
    }
    implicit def tuple3[T1, T2, T3, V, R] = Extender[(T1, T2, T3), V, (T1, T2, T3, V)]{
      case ((t1, t2, t3), v) => (t1, t2, t3, v)
    }
    implicit def tuple4[T1, T2, T3, T4, V, R] =
      Extender[(T1, T2, T3, T4), V, (T1, T2, T3, T4, V)]{
        case ((t1, t2, t3, t4), v) => (t1, t2, t3, t4, v)
      }
    // ... and so on until tuple21 ...
  }

  // C
  class FuncAb[A, B](val convert: A => B) {
//    def convert3 = (a: A) => List[B]
  }

  def convertToJson0(x: Any): Json = {
    x match{
      case s: String => Json.Str(s)
      case d: Double => Json.Num(d)
      case i: Int => Json.Num(i.toDouble)
      // maybe more cases for float, short, etc.
    }
  }

  trait Jsonable[T]{
    def serialize(t: T): Json
  }
  object Jsonable{
    implicit object StringJsonable extends Jsonable[String]{
      def serialize(t: String) = Json.Str(t)
    }
    implicit object DoubleJsonable extends Jsonable[Double]{
      def serialize(t: Double) = Json.Num(t)
    }
    implicit object IntJsonable extends Jsonable[Int]{
      def serialize(t: Int) = Json.Num(t.toDouble)
    }
  }

  def convertToJson[T](x: T)(implicit converter: Jsonable[T]): Json = {
    converter.serialize(x)
  }

  def main(args: Array[String]): Unit = {
    println("Activate ..")

    val i2str = new FuncAb[Int, String](_.toString)
    println("Int to String =>" + i2str.convert(100).length)

    println("WD-F-> " + widen(1.23f))
    println("WD-I-> " + widen(13))

    // -----

    val t = (1, "lol")
    val bigger = (extend(t, true))

    println("bigger tuple -> " + bigger)

    println("Json Object ->" + convertToJson(99))

  }
}



sealed trait Json
object Json{
  case class Str(s: String) extends Json
  case class Num(value: Double) extends Json
  // ... many more definitions
}
