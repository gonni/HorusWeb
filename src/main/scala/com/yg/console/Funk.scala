package com.yg.console

sealed abstract class Funk[+T] {

}

object Funk {
  def apply[T](r: => T): Funk[T] = {
    println("Detected Funk ..")
    Tuccess(r)
  }
}

final case class Tuccess[+T](value: T) extends Funk[T] {}