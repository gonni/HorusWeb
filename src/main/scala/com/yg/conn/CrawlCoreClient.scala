package com.yg.conn

import akka.actor.ActorSystem
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.jackson.Serialization.write

object CrawlCoreClient extends App {
//  implicit val system = ActorSystem()

  println("Active JS System ..")
  implicit val formats: DefaultFormats = DefaultFormats
//  val sam = Person("Jeff", 33)
//  val jsString = write(sam)
//  println(s"jsString -> ${jsString}")

  def crawlListUrl(crawlOption : CrawlPageWrapOption) : String = {
    val body = CrawlPageWrapOption(
      "https://news.naver.com/main/list.naver?mode=LS2D&sid2=263&sid1=101&mid=sec&listType=title&date=20220621&page=1",
      "^(https://n.news.naver.com/mnews/).*$",
      "ul.type02"
    )

    val jsBody = write(body)
    println(s"Body data to send -> ${jsBody}")
    val response = CommonConn.writeData2("http://localhost:8070/crawl/page/list", jsBody)

    println(s"Response Body -> ${response}")
    response
  }

  crawlListUrl(null)
}

case class CrawlPageWrapOption(targetSeedUrl: String, filterCrawlUrlRxPattern: String, filterDomGroupAttr: String)
