package com.yg.sample

object Main {
  def main(args: Array[String]): Unit = {
    import AaAir._
    val a2Air = new AaAir()
    a2Air.giveMeMoney(10)

    println("show ->" + Show.show(10))

    println("tuple show ->" + Show.show((1,2)))
  }
}
