package com.yg.temp

object AntiImplicits {

  def processLists[A, B](la: List[A], lb: List[B]): List[(A, B)] =
    for {
      a <- la
      b <- lb
    } yield (a, b)

  def processListsSameType[A](la: List[A], lb: List[A]): List[(A, A)] =
    processLists(la, la)

  def proecessListSameTypeV2[A, B](la: List[A], lb: List[B])(implicit evidence: A =:= B): List[(A, B)] =
    processLists(la, lb)

  trait =!=[A, B]
  def processDifferentTypes[A, B](la: List[A], lb: List[B])(implicit evidence: A =!= B): List[(A, B)] =
    processLists(la, lb)


  def main(args: Array[String]): Unit = {
    println("Active")
    val a = proecessListSameTypeV2(List(1,2,3), List(4,5,6))
    a.foreach(println(_))


  }
}
