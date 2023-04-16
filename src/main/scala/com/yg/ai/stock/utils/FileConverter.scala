package com.yg.ai.stock.utils

import java.io.{File, PrintWriter}
import scala.io.Source

object FileConverter {


  case class StockData(date: String, symbol: String, open: String, close: String,
                       low: String, high: String, volume: String)

  def main(args: Array[String]): Unit = {
    println("Active ..")

    val srcFile = Source.fromFile("datafile/^KS11.csv")
    val srcStockData = srcFile.getLines().filter(!_.contains("null")).map(line => {
      val tokens = line.split("\\,")
      StockData(tokens(0), "KOSPI", tokens(1), tokens(5), tokens(3), tokens(2), tokens(6))
    }).toSeq

    val writer = new PrintWriter(new File("datafile/kospi.csv"))
    writer.println("date,symbol,open,close,low,high,volume")
    //date,symbol,open,close,low,high,volume

    srcStockData.zipWithIndex.filter(_._2 > 0).foreach(sdi => {
      val sd = sdi._1
      writer.printf("%s,%s,%s,%s,%s,%s,%s\n", sd.date, "KOSPI", sd.open, sd.close, sd.low, sd.high, sd.volume)
    })

    println(s"total data : %d", srcStockData.size)
    println("Completed ..")

    writer.close()

    srcFile.close()
  }
}
