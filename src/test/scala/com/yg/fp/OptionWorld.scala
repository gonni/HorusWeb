package com.yg.fp

object OptionWorld {
  def parseInsuranceRate(age: String,
                         numberOfSpeedingTickets: String): Option[Double] = {
    val optAge: Option[Int] = Try(age.toInt)
    val optionTicket: Option[Int] = Try(numberOfSpeedingTickets.toInt)
    map22(optAge, optionTicket)(insureanceRateQuote)
  }

  def insureanceRateQuote(age: Int, numberOfSpeedingTickets: Int) : Double = {
    age / numberOfSpeedingTickets
  }

  def map2[A,B,C](a: Option[A], b: Option[B])(f: (A, B) => C) : Option[C] =
    a flatMap (aa =>
      b map (bb =>
        f(aa, bb)
        ))

  def map22[A,B,C](a: Option[A], b: Option[B])(f: (A, B) => C) : Option[C] =
    for{
      aa <- a
      bb <- b
    } yield f(aa, bb)

  def Try[A](a: => A): Option[A] =
    try Some(a)
    catch {case e: Exception => None}

  def main(args: Array[String]): Unit = {
    println("Magic Value ->" + parseInsuranceRate("43", "7"))
  }
}
