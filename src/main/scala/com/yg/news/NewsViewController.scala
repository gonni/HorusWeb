package com.yg.news

import com.yg.data.DbHorusCrawled
import com.yg.data.DbHorusCrawled.CrawlUnit
import org.scalatra.forms.FormSupport
import org.scalatra.i18n.I18nSupport
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait NewsViewProcessing extends ScalatraServlet
  with FormSupport with I18nSupport with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  // latest newslist list
  get("/hot") {
    val seedNo = params("seedNo").toInt
    logger.info(" -> Request Hot-News {}", seedNo)

    val res : Future[Seq[CrawlUnit]] = db.run[Seq[CrawlUnit]](DbHorusCrawled.findAll(seedNo).result)
    val syncRes = Await.result(res, Duration.Inf)

    val newsPage =com.yg.news.html.newslist.render(syncRes)
    layouts.html.dashboard.render("News", newsPage)
  }

  // newslist details
//  get("/details") {
//    logger.info("")
//    val newsPage = com.yg.news.html.newsview.render()
//    layouts.html.dashboard.render("News Datail", newsPage)
//  }

}

class NewsViewController(val db: Database) extends ScalatraServlet with FutureSupport with NewsViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
