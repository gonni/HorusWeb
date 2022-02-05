package study
import ExImplicit1._

class PreferredPrompt(val preference: String)
class PreferredDrink(val preference: String)

object Greeter {

  def greet(name: String)(implicit prompt: PreferredPrompt, drink: PreferredDrink) = {
    println(s"Your name : ${name}")
    println(s" - prompt : ${prompt}")
    println(s" - drink : ${drink}")
  }

//  def main(args: Array[String]): Unit = {
//    Greeter.greet("Kona")
//  }
}

object ExImplicit1 {
  implicit val prompt = new PreferredPrompt("My Master >");
  implicit val drink = new PreferredDrink("beer")


}
