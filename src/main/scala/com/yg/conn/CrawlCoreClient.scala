package com.yg.conn

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.slf4j.LoggerFactory

object CrawlCoreClient {
  val logger =  LoggerFactory.getLogger(getClass)
  implicit val formats: DefaultFormats = DefaultFormats

  def crawlListUrl(crawlOption : CrawlListWrapOption) : String = {
    val jsBody = write(crawlOption)
    logger.info(s"ListPage body data to send -> ${jsBody}")
    val response = CommonConn.writeData2("http://localhost:8070/crawl/page/list", jsBody)

    logger.info(s"Response Body -> ${response}")
    response
  }

  def crawlContentPage(crawlOption: CrawlContentWrapOption) = {
    val jsBody = write(crawlOption)
    logger.info(s"ContentPage body data to send -> ${jsBody}")
    val response = CommonConn.writeData2("http://localhost:8070/crawl/page/content", jsBody)

    logger.info(s"Response Body -> ${response}")
    response
  }

}

case class CrawlListWrapOption(
                                targetSeedUrl: String,
                                filterCrawlUrlRxPattern: String,
                                filterDomGroupAttr: String
                              )

case class CrawlContentWrapOption(
                                 targetUrl: String,
                                 docTitle: String,
                                 docDatetime: String,
                                 contentGrp: String
                                 )