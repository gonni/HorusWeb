package com.yg.console

import scala.util.Try

object FutureMain extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  def getUrlSpec(): Future[Seq[String]] = Future {
    val f = Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt")
    try f.getLines.toList finally f.close()
  }

  val urlSpec: Future[Seq[String]] = getUrlSpec()

  def find(lines: Seq[String], word: String) = lines.zipWithIndex collect {
    case (line, n) if line.contains(word) => (n, line)
  } mkString("\n")

  urlSpec foreach {
    lines => log(s"Found occurrences of 'telnet'\n${find(lines, "telnet")}\n")
  }

  urlSpec foreach {
    lines => log(s"Found occurrences of 'password'\n${find(lines, "password")}\n")
  }

  log("callbacks installed, continuing with other work")
  Thread.sleep(1000 * 1)

  def log(s: Any): Unit = {
    Console.println(s)
  }

  println("Ramda Function ..")

  funBody(() => 2)

  foreach2 {
    a => println("AA")
  }

  def funBody(f:()=>(Int)): Int = {
    val a = f()
    println(s"FunBody .. ${a}")
    a
  }

  def funBody2[U](f:(Int)=>U)(n:Int=1) : Unit = {
    println("Test funBody2 ..")
  }

  def foreach2[U](f: (Int) => U): Unit = {
    println("Detected Foreach ..")
  }

  val a = List (1,2,3)

  val b = List('a','b','c')

  println(a.zip(b))     // List( (1,'a'), (2,'b') , (3,'c') )   * 두 리스트를 순서쌍으로 묶는다.
  println(b.zipWithIndex)
//  b.zipWidhIndex   // List ( ('a',0), ('b',2), ('c', 3) )
  println(b.toString)
  println("mkString ->" + b.mkString( "[" , "," , "]"))

  val words = List ( "the" , "quick" , "brown" , "fox" )
  println(words.map(_.toList))      // List (List (t,h,e) , List (q,u,i,  .........
  println(words.flatMap(_.toList))   //  List (t,h,e,q,u,i ...........

  val map = Map(1 -> "one", 2 -> "two", 3 -> "three")

  println( 1 to 2 flatMap(map.get))

  def toInt(s: String): Option[Int] = {
    try {
      Some(Integer.parseInt(s.trim))
    }
    catch {
      case e: Exception => None
    }
  }

  val ints = List(List(1,2,3), List(4,5))
  println("-->" + ints.flatMap(_.toString))
  println("==>" + ints.flatMap(_.map(_.toString)))

  println("===>" + ints.flatten)
  println("__>" + ints.flatMap(v => v))
  println("++>" + ints.flatMap(_.map(m => m)))
  println("-->" + ints.flatMap(_.map(_.toString)));

  def f(i: Int) : String = i.toString
  def g = (x:Int) => f(x)
  def g2 = f _
  def f2 = (_:String).toString

  def u(i: Int)(d: Double) : String = i.toString + d.toString

  def v = u _
  def w1 = u(9) _
  println(w1(12345.12345))

  def w2 = u(_:Int)(2.0)
  println(w2(3))

  val strList = ("foo", "bar", "boo")
  println(strList)
  println("__>" + strList._1)

  Try{
    println("AA")
  }

  Funk{
    println("Hello Funk")
  }
//  val s = "Hello"
//  val f: Future[String] = Future {
//    s + " future!"
//  }
//  f foreach {
//    msg => println(msg)
//  }
//
//  f.foreach{println}
//
//  println(1 to 5)
}
