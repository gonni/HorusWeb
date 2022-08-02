package com.yg.news

import com.yg.conn._
import com.yg.data.CrawledRepo.CrawlUnit
import com.yg.data.{CrawlContentWrapOption, CrawlListWrapOption, CrawledRepo}
import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

trait NewsDataProcessing extends ScalatraServlet with JacksonJsonSupport with FutureSupport {
  val logger =  LoggerFactory.getLogger(getClass)
  override protected implicit def jsonFormats: Formats = DefaultFormats.withBigDecimal

  def db: Database

  before() {
    contentType = formats("json")
  }

  get("/news/details") {
    val clientIp = request.getRemoteAddr
    val newsId = params("newsId").toLong
    logger.info("Detected API NewsDetails: {} from {}", newsId, clientIp)
    val dbRes = db.run(CrawledRepo.getCrawledData(newsId).result)
    val newsData = Await.result(dbRes, Duration.Inf).headOption
    logger.info("NewsData : {}", newsData)

    newsData.getOrElse(CrawlUnit(crawlNo = newsId, status = Option("NotExist") ))
  }

  get("/api/news/click") {

  }


}

class NewsJsController(val db: Database) extends ScalatraServlet with FutureSupport with NewsDataProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}