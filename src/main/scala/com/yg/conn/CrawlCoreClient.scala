package com.yg.conn

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.jackson.Serialization.write

object CrawlCoreClient extends App {

  println("Active JS System ..")
  implicit val formats: DefaultFormats = DefaultFormats
//  val sam = Person("Jeff", 33)
//  val jsString = write(sam)
//  println(s"jsString -> ${jsString}")

  def crawlListUrl(crawlOption : CrawlPageWrapOption) : String = {



    "TBD"
  }
}

case class CrawlPageWrapOption(targetUrl: String, grabUrlPattern: String, grpDomPath: String)
