package com.yg.news

import com.yg.conn._
import com.yg.data.{CrawlContentWrapOption, CrawlListWrapOption}
import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.slf4j.LoggerFactory

class NewsJsController extends ScalatraServlet with JacksonJsonSupport {
  val logger =  LoggerFactory.getLogger(getClass)
  override protected implicit def jsonFormats: Formats = DefaultFormats.withBigDecimal

  before() {
    contentType = formats("json")
  }

  get("/api/news/details") {
    val newsId = params("newsId").toInt
    logger.info("Detected API NewsDetails: {}", newsId)



  }

  get("/api/news/click") {

  }


}
