package com.yg.news

import com.yg.conn.InfluxClient.TermCount
import com.yg.conn.{CrawlCoreClient, InfluxClient, MabScore}
import com.yg.data.{CrawledRepo, NewsRepo}
import com.yg.data.CrawledRepo.CrawlUnit
import com.yg.data.NewsRepo.NewsClick
import com.yg.processing.TopicAnalyzer
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
    val cnt = params("cnt").toInt
    logger.info(s"Requested recommended news ..${clientIp}")

    val res : Future[Seq[CrawlUnit]] = db.run[Seq[CrawlUnit]](CrawledRepo.findAll(9).take(cnt).result)
    val syncRes = Await.result(res, Duration.Inf)

    // insert show item
    syncRes.foreach(cu => {
      db.run(NewsRepo.insertClickLog(Seq(NewsClick(userId = clientIp, pageCd = "ROPN", newsId = cu.crawlNo.toInt))))
    })

    val mabScores = CrawlCoreClient.mabScoreJs(cnt)

    val mergedUnit  = syncRes.flatMap(r => mabScores.map(s => {
      if(String.valueOf(r.crawlNo) == s.itemId) {
        MergedUnit(r, s)
      }
    })).filter(r => r != ()).collect {case a: MergedUnit => a}

    val newsPage =com.yg.news.html.recoNews.render(mergedUnit)
    layouts.html.dashboard.render("Pick 5", newsPage)
  }

  // TODO need to add removing stop words
  get("/termMonitor") {
    val clientIp = request.getRemoteAddr
    val seedNo = params("seedNo").toLong
    logger.info(s"Requested recommended news ..${clientIp}")
    val highTerms = InfluxClient.getHighTerms("-1h", 30)
    val ta = new TopicAnalyzer(db)
    val stopWords = ta.loadStopWords(-10)
    val stopRemoved = highTerms.filter(ht => {!stopWords.contains(ht.term)})

    logger.info("highTerms -> " + stopRemoved.mkString(" | "))

    val mainPage = com.yg.news.html.newsTerm.render(stopRemoved, seedNo)
    layouts.html.dashboard.render("NewsStream", mainPage)
  }

  // TODO need to add removing stop words
  get("/topicMonitor") {
    val seedNo = params("seedNo").toInt
    val clientIp = request.getRemoteAddr
    logger.info(s"Requested recommended news ..${clientIp}")

    val ta = new TopicAnalyzer(db)
    val targetTerms = ta.topicTermDics(seedNo, 7).map(topic => {
      topic.map(lda => {
        lda._1.term
      }).mkString("|")
    })

    targetTerms.foreach(topicStr => {
      logger.info("TopicMonBase : " + topicStr)
    })

    val mainPage = com.yg.news.html.streamShow.render(targetTerms.map(t => TermCount(t, 0)))
    layouts.html.dashboard.render("TopicView", mainPage)
  }

  get("/topics") {
    logger.info("detected topic viewer ..")

    layouts.html.dashboard.render("Topic Cell", com.yg.news.html.newsTopic.render())
  }

  get("/net") {
    logger.info("detected wordnet ..")

    layouts.html.dashboard.render("Term Net", com.yg.news.html.wordnet.render())
  }

  get("/link") {
    logger.info("detected wordlink ..")

    layouts.html.dashboard.render("Term Link 3D", com.yg.news.html.wordlink.render())
  }

  get("/multiLink") {
    logger.info("detected multiLink ..")
    layouts.html.dashboard.render("Term Link 3D", com.yg.news.html.multiWordLink.render())
  }

}
case class MergedUnit (crawlUnit: CrawlUnit, mabScore: MabScore)

class NewsViewController(val db: Database) extends ScalatraServlet with FutureSupport with NewsViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
