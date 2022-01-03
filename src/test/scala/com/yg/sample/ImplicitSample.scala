package com.yg.sample

object ImplicitSample {
  sealed trait Json
  object Json{
    case class Str(s: String) extends Json
    case class Num(value: Double) extends Json
    case class List(items: Json*) extends Json
    // ... many more definitions
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
    implicit object FileJsonable extends Jsonable[java.io.File]{
      def serialize(t: java.io.File) = Json.Str(t.getAbsolutePath)
    }
    implicit def SeqJsonable[T: Jsonable]: Jsonable[Seq[T]] = new Jsonable[Seq[T]]{
      def serialize(t: Seq[T]) = {
        Json.List(t.map(implicitly[Jsonable[T]].serialize):_*)
      }
    }
  }

//  def convertToJson[T](x: T)(implicit converter: Jsonable[T]): Json = {
//    converter.serialize(x)
//  }

  def convertToJson2[T: Jsonable](x: T): Json = {
    implicitly[Jsonable[T]].serialize(x)
  }

  def convertToJson[T: Jsonable](x: T): Json = {
    implicitly[Jsonable[T]].serialize(x)
  }
  def convertToJsonAndPrint[T: Jsonable](x: T) = println(convertToJson(x))
  def convertMultipleItemsToJson[T: Jsonable](t: Array[T]) = t.map(convertToJson(_))

  def main(args: Array[String]): Unit = {
    println("Active ..")

    println(convertToJson("AAA+"))
    println(convertToJson(11))
    println(convertToJson2(19.9))
    println(convertToJson(Seq(1,2,3)))
    println(convertToJson(Seq(Seq(Seq(1, 2, 3)))))
//    val x : Seq[Seq[Int]] = Seq(Seq(1),Seq(2),Seq(3))
//
//    def f(arg: Seq[Any]*) : Int = {
//      arg.length
//    }
//
//    println("->" + f(x)) //1 as x is taken as single arg
//    println("=>" + f(x:_*))  // 2 as x is "unpacked" as a Seq[Any]*


  }
}
