package com.yg.temp


object Chap21 {
  // implicit class
  case class Rectanlge(width: Int, height: Int)
  implicit class RectanlgeMaker(width: Int) {
    def x (height: Int) = Rectanlge(width, height)
  }

  // implicit parameter
  object JoesPrefs {
    implicit val prompt = new PreferredPrompt("Yes, masger> ")
  }

  def main(args: Array[String]): Unit = {
    import JoesPrefs._
    println("Active .. ")

    val mySizedRectangle = 3 x 4


    Greeter.greet("Joe")(new PreferredPrompt("AA >"))
    Greeter.greet("Marx")

  }
}

// implicit parameter
class PreferredPrompt(val preference: String)
object Greeter {
  def greet(name: String)(implicit prompt: PreferredPrompt) = {
    println("Welcome, " + name + ". This system is ready.")
    println(prompt.preference)
  }
}


