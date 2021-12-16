package com.yg.temp

object TypeLevelProgramming extends App {
//  val a = (1, 2, "AAA")
//  println(a._1)
//  println(a.productIterator.toList)
//
//  val b = new Vtable[(Int, String, String, String)]("METANFA")
//  b.process()
  implicit val intOrd: Ord[Int] = new Ord[Int] {
    override def cmp(me: Int, you: Int): Int = me - you
  }


  println("--> " + intOrd.cmp(1, 2))

  implicit def twoOrd[X,Y](implicit ordX : Ord[X], ordY : Ord[Y])  : Ord[(X,Y)] = new Ord[(X,Y)]{
    def cmp(me : (X,Y), you : (X,Y)) : Int = {
      val c1 = ordX.cmp(me._1, you._1)
      if(c1 != 0) c1
      else ordY.cmp(me._2 , you._2)
    }
  }

  println("==>" + twoOrd.cmp((1,2), (1,10)))


  def max3[A](a : A, b : A, c: A)(implicit ord : Ord[A]) : A =
    if (ord.<=(a,b))  {if (ord.<=(b,c)) c else b}
    else              {if (ord.<=(a,c)) c else a }

  println("++>" + max3(1,3,2))

  implicit val intIter : Iter[Int, Int] = new Iter[Int, Int]{
    def getValue(i : Int) = if (i>0) Some(i) else None
    def getNext(i : Int) = if(i-1 > 0 ) i-1 else 0
  }

  println("__>" + intIter.getValue(10))

  implicit val listIter2 : Iter2[List] = new Iter2[List]{
    def getValue[A](a : List[A]) = a.headOption
    def getNext[A](a : List[A]) = a.tail
  }

  println("^^>" + listIter2.getValue(List(3,1,2)))

  implicit val ListFunctor : Functor[List] = new Functor[List]{
    def map[A,B](f : A=>B)(x : List[A]) : List[B] = x.map(f)
  }

  implicit val TreeFunctor : Functor[MyTree] = new Functor[MyTree]{
    def map[A,B](f : A=>B)(x : MyTree[A]) : MyTree[B] = x match{
      case Empty() => Empty()
      case Node(v, left, right) => Node(f(v), map(f)(left), map(f)(right))
    }
  }

  def compose[F[_],X,Y,Z](g : Y=>Z)(f : X=>Y)(xs : F[X])(implicit proxy : Functor[F]) : F[Z] ={
    proxy.map(g)(proxy.map(f)(xs))
  }
}

trait Functor[F[_]] {
  def map[A, B](f : A=>B)(x : F[A]) : F[B]
}

sealed abstract class MyTree[A]
case class Empty[A]() extends MyTree[A]
case class Node[A](value : A, left : MyTree[A], right : MyTree[A]) extends MyTree[A]

abstract class Iter2[I[_]]{
  def getValue[A](a : I[A]) : Option[A]
  def getNext[A](a : I[A]) : I[A]
}

abstract class Iter[I, A]{
  def getValue(i : I) : Option[A]  //메서드의 디테일은 인스턴스에서 구현합니다.
  def getNext(i : I) : I
}

class Vtable[T](tableName: String) {
  final type TableElement = T

  def process(): Unit = {
//    val a : TableElement = ???
    println("process")
  }

}

abstract class Ord[A]{
  def cmp(me : A, you : A) : Int

  def ===(me : A, you : A) : Boolean = cmp(me, you) == 0
  def <(me : A, you : A) : Boolean = cmp(me, you) < 0
  def >(me : A, you : A) : Boolean = cmp(me, you) > 0
  def <=(me : A, you : A) : Boolean = cmp(me, you) <= 0
  def >=(me : A, you : A) : Boolean = cmp(me, you) >= 0
}