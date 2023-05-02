package study
import ExImplicit1._

object Main  {
  def main(args: Array[String]): Unit = {
    val dateSample = "2023.01.17"
    dateSample.split("\\.").foreach(println)

  }
}
