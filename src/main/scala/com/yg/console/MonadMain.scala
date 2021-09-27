package com.yg.console

object MonadMain extends App {
  println("Hell Monad")


  def f : Int => Boolean = (a: Int) => if(a>0) true else false
  def g : Boolean => String = (b: Boolean) => if(b) "Good" else "Bad"

  def h(c:Int) : String = g(f(c))
}
