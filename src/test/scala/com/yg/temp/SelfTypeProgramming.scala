package com.yg.temp

object SelfTypeProgramming extends App {
  println("Active System ..")
  val realBayon = new VerifiedTweeter("Bayon")
  realBayon.tweet("Vonejure")
}

trait User {
  def username: String
}

trait Tweeter {
  this: User =>
  def tweet(tweetText: String) = println(s"$username: $tweetText")
}

class VerifiedTweeter(val username_ :String) extends Tweeter with User {
  def username = s"real $username_"
}