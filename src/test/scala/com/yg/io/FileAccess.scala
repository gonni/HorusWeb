package com.yg.io

import java.io.{BufferedReader, File, FileReader}
import scala.io.Source
import scala.runtime.Nothing$
import scala.util.{Failure, Success, Try}

object FileAccess {

  def printFile(file: String): Unit = {
    try {
      for(line <- Source.fromFile(file).getLines()) {
        println(line)
      }
    } catch {
      case ex: Exception => println(ex)
    } finally {
      println("IO completed ..")
    }
  }

  def main(args: Array[String]): Unit = {
    println("Active IO")
//    var br : BufferedReader = null
//    try {
//      println("AbsPath = " + new File(".").getAbsolutePath)
//      br = new BufferedReader(new FileReader("/Users/a1000074/IdeaProjects/scalaRes/README.md"))
////      println("line ->" + br.readLine())
//      br.lines().forEach(println)
//      println("File Read Completed ..")
//    } catch {
//      case ex: Exception => println(ex)
//    } finally {
//      if(br != null) br.close()
//    }

    printFile("README.md")
  }
}
