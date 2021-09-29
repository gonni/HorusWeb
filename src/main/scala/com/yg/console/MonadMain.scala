package com.yg.console

import com.sun.xml.internal.ws.util.CompletedFuture

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
object MonadMain extends App {
  println("Hell Monad")


  def f : Int => Boolean = (a: Int) => if(a>0) true else false
  def g : Boolean => String = (b: Boolean) => if(b) "Good" else "Bad"

  def h(c:Int) : String = g(f(c))

  val s = "Hell"
  val fu  : Future[String] = Future {
    s + "future"
  }

  fu onComplete {
    case Success(msg) => println(s"Message from future : $msg")
    case Failure(e) => e.printStackTrace
  }

  val zenMasters = List(
    Person("YG"),
    Person("Kuma")
  )

  def getStockPrice(stockSymbol: String): Future[Double] = Future {
    Thread.sleep(3000L)
    println(s"Future work completed ${stockSymbol}")
    12d
  }

  val kospi = getStockPrice("KOSPI")

  kospi.onComplete {
    case Success(x) => {
      println(s"Value from Future => ${x}")
    }
    case Failure(e) => e.printStackTrace()
  }


    zenMasters foreach(x => println(x.name))

  def toInt(s: String): Option[Int] = {
    try {
      Some(Integer.parseInt(s.trim))
    } catch {
      case e: Exception => None
    }
  }

  def toIntTry(s: String): Try[Int] = Try {
    Integer.parseInt(s.trim)
  }

  toInt("777a") match {
    case Some(a) => println(a)
    case None => println("Error ..")
  }

  val aT = toIntTry("a77")
  println("toIntTry ->" + aT)

  aT match {
    case Success(i) => println(i)
    case Failure(e) => println(s"Fained, Reaseon: $e")
  }

  val y = for{
    a <- toInt("5")
    b <- toInt("6a")
    c <- toInt("7")
  } yield a + b + c

  println("sum ->" + y.flatMap(a => Option(a)))

  val f1: ((Int, Int)) => Int = { case (a, b) => a + b }

  println(s"f1 = ${f1(1, 2)}")

  Thread.sleep(10000L)
}

class Person {
  var name = ""
}

object Person {
  def apply(name: String): Person = {
    var p = new Person
    p.name = name
    p
  }
}
