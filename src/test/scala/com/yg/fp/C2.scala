package com.yg.fp

object C2 {


  def main(args: Array[String]): Unit = {
    case class CaseC1(first: String, last: String)
    val cc = CaseC1("Jeff", "Kim")
    println(CaseC1.apply("Jeff", "Kim"))
    println(CaseC1.tupled)
//    CaseC1.unapply
  }
}



abstract class C2 {
  def partial1[A,B,C](a: A, f: (A,B) => C): B => C
}

class ConcC2 extends C2{
  override def partial1[A, B, C](a: A, f: (A, B) => C): B => C = (b: B) => f(a, b)
}