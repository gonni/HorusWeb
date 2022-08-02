package com.yg.cwl

import com.yg.data.SeedWrapRepo.{CrawlSeed, WrapperRule, crawlSeeds, insertNewSeed, insertNewSeedAndGetId, insertWrapperRule}
import com.yg.data.{CrawlContentWrapOption, CrawlContentWrapOptionConf, CrawlListWrapOption, CrawlListWrapOptionConf, SeedWrapRepo}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.scalatra.forms.FormSupport
import org.scalatra.i18n.I18nSupport
import org.scalatra.json.JacksonJsonSupport
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import java.sql.Timestamp
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

trait AdminDataProcessing extends ScalatraServlet
  with I18nSupport with FutureSupport with JacksonJsonSupport {
  val logger = LoggerFactory.getLogger(getClass)
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  def db: Database

  before() {
    contentType = formats("json")
  }

  case class Response(status: String, seedNo: Int)
  // Config
  post("/crawl/list/wrapper/save") {
    val req = parsedBody.extract[CrawlListWrapOptionConf]
    logger.info(s"req: ${req}")

    val res = db.run(insertNewSeedAndGetId(req.crawlName, req.targetSeedUrl, "INIT"))
    val newSeedNo = Await.result(res, 10.seconds)
    logger.info(s"Created new seedNo :${newSeedNo}")

    val wRules = Seq(
      WrapperRule(None, newSeedNo, "LIST_URL_PATTERN_FILTER", req.filterCrawlUrlRxPattern,
        "ListCrawlUrlPattern", new Timestamp(System.currentTimeMillis())),
      WrapperRule(None, newSeedNo, "LIST_URL_TOP_AREA_FILTER", req.filterDomGroupAttr,
        "ListCrawlGroup", new Timestamp(System.currentTimeMillis()))
    )
    db.run(insertWrapperRule(wRules))

    Response("SUCC", newSeedNo)
  }

//  def insertNewSeed1(title: String, url: String, status: String) = db.run {
//    (crawlSeeds returning crawlSeeds.map(_.seedNo)) += CrawlSeed(None, url, title, status)
//  }

  post("/crawl/content/wrapper/save") {
    val req = parsedBody.extract[CrawlContentWrapOptionConf]
    logger.info(s"req: ${req}")
    val newSeedNo = req.seedNo

    val wRules = Seq(
      WrapperRule(None, newSeedNo, "CONT_TITLE_ON_PAGE", req.docTitle,
        "ListCrawlUrlPattern", new Timestamp(System.currentTimeMillis())),
      WrapperRule(None, newSeedNo, "CONT_MAIN_CONT", req.contentGrp,
        "ListCrawlGroup", new Timestamp(System.currentTimeMillis())),
      WrapperRule(None, newSeedNo, "CONT_DATE_ON_PAGE", req.docDatetime,
        "ListCrawlGroup", new Timestamp(System.currentTimeMillis()))
    )
    db.run(insertWrapperRule(wRules))

    Response("SUCC", newSeedNo)
  }

}

class CrawlConfController (val db: Database) extends ScalatraServlet
  with FutureSupport with AdminDataProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
