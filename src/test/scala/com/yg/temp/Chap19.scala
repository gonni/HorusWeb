package com.yg.temp

object Chap19 extends App {
  println("Active ..")

  val a = new QueueV1[String]
  val b = new QueueV1[String]("A", "B", "C")
  println("b => " + b)
  b.print
}



class QueueV1[T] private (
                 private val leading: List[T],
                 private val trailing: List[T]
                 ){
  private def mirror =
    if(leading.isEmpty)
      new QueueV1(trailing.reverse, Nil)
    else
      this

  def this() = this(Nil, Nil)
  def this(elems: T*) = this(elems.toList, Nil)

  def print() = {
    println(leading)
    println(trailing)
  }

  def head = mirror.leading.head

  def tail = {
    val q = mirror
    new QueueV1(q.leading.tail, q.trailing)
  }

  def enqueue(x: T) =
    new QueueV1(leading, x :: trailing)
}

object Ex01 {
  def orderedMergeSort[T <: Ordered[T]](xs: List[T]): List[T] = {
    ???
  }
}

class Person(val firstName: String, val lastName: String) extends Ordered[Person] {
  override def compare(that: Person): Int = ???
  override def toString = ???
}
