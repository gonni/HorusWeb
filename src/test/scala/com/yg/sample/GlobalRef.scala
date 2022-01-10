package com.yg.sample

object GlobalRef {

}

class AaAir {
  def giveMeMoney (a: String): String = {
    "Your money is " + a
  }
}
object AaAir {
  implicit def convertIntToWon(a: Int) : String = a.toString + "WON"
}

trait Show[A] {
  def show(a : A) : String
}

object Show {
  def show[A](a: A)(implicit sh: Show[A]) = sh.show(a)
  implicit val intCanShow: Show[Int] =
    new Show[Int] {
      def show(int: Int): String = s"int $int"
    }
  implicit val tuple2ConShow: Show[(Int, Int)] =
    new Show[(Int, Int)] {
      override def show(a: (Int, Int)): String = s"Tupled Ints ${a._1} / ${a._2}"
    }
}