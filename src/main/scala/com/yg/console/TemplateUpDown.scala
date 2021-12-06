package com.yg.console

object TemplateUpDown extends App {
  println("Active ..")

  val x: Foo[List]#t[Int] = List(1)

  println(x)
}

trait Foo[M[_]] {
  type t[A] = M[A]
}


trait WithMap[F[_]] {
  def map[A,B](fa: F[A])(f: A => B): F[B]
}


trait Bar[F[_]] {
  def g[F[_]] = println("Working? ")
}

