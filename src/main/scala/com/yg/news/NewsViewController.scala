package com.yg.news

import com.yg.data.{CrawledRepo, NewsRepo}
import com.yg.data.CrawledRepo.CrawlUnit
import com.yg.data.NewsRepo.NewsClick
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

    val res : Future[Seq[CrawlUnit]] = db.run[Seq[CrawlUnit]](CrawledRepo.findAll(seedNo).result)
    val syncRes = Await.result(res, Duration.Inf)

    val newsPage =com.yg.news.html.newslist.render(syncRes)
    layouts.html.dashboard.render("News", newsPage)
  }

  get("/pick") {
    val clientIp = request.getRemoteAddr
    logger.info(s"Requested recommended news ..${clientIp}")

    val res : Future[Seq[CrawlUnit]] = db.run[Seq[CrawlUnit]](CrawledRepo.findAll(9).take(5).result)
    val syncRes = Await.result(res, Duration.Inf)

    syncRes.foreach(cu => {
      db.run(NewsRepo.insertClickLog(Seq(NewsClick(userId = clientIp, pageCd = "ROPN", newsId = cu.crawlNo.toInt))))
    })

//    db.run(NewsRepo.insertClickLog(Seq(NewsClick(userId = clientIp, pageCd = "RCLK", newsId = newsId))))

    val newsPage =com.yg.news.html.recoNews.render(syncRes)
    layouts.html.dashboard.render("Pick 5", newsPage)
  }
}

class NewsViewController(val db: Database) extends ScalatraServlet with FutureSupport with NewsViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
