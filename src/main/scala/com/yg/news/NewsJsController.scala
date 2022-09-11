package com.yg.news

import com.yg.conn._
import com.yg.data.CrawledRepo.CrawlUnit
import com.yg.data.NewsRepo.NewsClick
import com.yg.data.{CrawlContentWrapOption, CrawlListWrapOption, CrawledRepo, NewsRepo}
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
    val newsId = params("newsId").toInt
    logger.info(s"Detected API NewsDetails: ${newsId} from ${clientIp}")
    val dbRes = db.run(CrawledRepo.getCrawledData(newsId).result)
    val newsData = Await.result(dbRes, Duration.Inf).headOption
    logger.info(s"NewsData : {}", newsData)

    //TODO need to impl db.run in transaction
    db.run(NewsRepo.insertClickLog(Seq(NewsClick(userId = clientIp, newsId = newsId))))

    newsData.getOrElse(CrawlUnit(crawlNo = newsId, status = Option("NotExist") ))
  }

  get("/news/pick/details") {
    val clientIp = request.getRemoteAddr
    val newsId = params("newsId").toInt
    logger.info(s"Detected API NewsDetails: ${newsId} from ${clientIp}")
    val dbRes = db.run(CrawledRepo.getCrawledData(newsId).result)
    val newsData = Await.result(dbRes, Duration.Inf).headOption
    logger.info(s"NewsData : {}", newsData)

    //TODO need to impl db.run in transaction
    db.run(NewsRepo.insertClickLog(Seq(NewsClick(userId = clientIp, pageCd = "RCLK", newsId = newsId))))

    newsData.getOrElse(CrawlUnit(crawlNo = newsId, status = Option("NotExist") ))
  }

  get("/news/analysis") {
    val clientIp = request.getRemoteAddr
    val newsId = params("newsId").toInt
    val news: CrawlUnit = getNewsData(newsId, clientIp)

    def getTopicScores(sentence: Option[String]): List[TopicScore] = {
      val topicResult = NlpCoreClient.getTopicScoreJs(sentence.getOrElse("NULL_INPUT"))
      topicResult.termScores.filter(_.score > 0.0).sortWith(_.score > _.score)
    }

    AnalyzedNews(news, getTopicScores(news.anchorText), getTopicScores(news.pageText).filter(_.score > 2.3))
  }

  case class AnalyzedNews(newsData: CrawlUnit,
                          anchorTopicScore : List[TopicScore],
                          contentTopicScore: List[TopicScore])

  def getNewsData(newsId: Int, clientIp: String) = {
    logger.info("Detected API NewsDetails: {} from {}", newsId, clientIp)
    val dbRes = db.run(CrawledRepo.getCrawledData(newsId).result)
    val newsData = Await.result(dbRes, Duration.Inf).headOption
    logger.info("NewsData : {}", newsData)

    //TODO need to impl db.run in transaction
    db.run(NewsRepo.insertClickLog(Seq(NewsClick(userId = clientIp, newsId = newsId))))

    newsData.getOrElse(CrawlUnit(crawlNo = newsId, status = Option("NotExist") ))
  }

}

class NewsJsController(val db: Database) extends ScalatraServlet with FutureSupport with NewsDataProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}