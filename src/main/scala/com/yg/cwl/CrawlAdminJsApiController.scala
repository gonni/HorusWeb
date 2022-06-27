package com.yg.cwl

import com.yg.conn.{CrawlCoreClient, CrawlPageWrapOption}
import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.slf4j.LoggerFactory

class CrawlAdminJsApiController extends ScalatraServlet with JacksonJsonSupport {
  val logger =  LoggerFactory.getLogger(getClass)
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  before() {
    contentType = formats("json")
  }

  post("/crawl/list") {
    val req = parsedBody.extract[CrawlPageWrapOption]
    logger.info(s"req: ${req}")
    CrawlCoreClient.crawlListUrl(req)
  }

  //TODO need to fix
  post("/crawl/content") {
    val req = parsedBody.extract[CrawlPageWrapOption]
    logger.info(s"req: ${req}")
    CrawlCoreClient.crawlListUrl(req)
  }

}