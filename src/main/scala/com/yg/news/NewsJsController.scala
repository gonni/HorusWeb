package com.yg.news

import com.yg.conn._
import com.yg.data.CrawledRepo.CrawlUnit
import com.yg.data.DtRepo.TermDist
import com.yg.data.NewsRepo.NewsClick
import com.yg.data.{CrawlContentWrapOption, CrawlListWrapOption, CrawledRepo, DtRepo, NewsRepo}
import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.slf4j.LoggerFactory
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._

import scala.collection.mutable
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

  get("/news/termdist") {
    logger.info("Detected Term-Dist data ..")
    val lt = Await.result(db.run(DtRepo.latestTdGrpTs1.head), Duration.Inf).grpGs
    println("Time Group => " + lt)
    val res = db.run(DtRepo.termDists(lt).result)
    val termDists = Await.result(res, Duration.Inf)

    val nodes = Await.result(db.run(DtRepo.getDistBaseTerm(lt).result), Duration.Inf)
    val setTerm = mutable.Set[String]()

    var dNodes = Array[Node](Node("NEWS", 1000))
    var dLink = Array[Link]()

    var i = 0;
    nodes.foreach(term => {
//      if(setTerm.contains(term)) {
//        println("Not Contain")
//      }
      i = i + 1
      setTerm += term
      dNodes = dNodes :+ Node(term, i)
      dLink = dLink :+ Link(term, "NEWS", 3)

      val comps = Await.result(db.run(DtRepo.getCompTerms(lt, term, 10)), Duration.Inf)
      for (elem <- comps) {
        if(!setTerm.contains(elem.compTerm)) {
          dNodes = dNodes :+ Node(elem.compTerm, i)
          setTerm += elem.compTerm
        } else {
          println(s"duplicated : ${elem.compTerm}")
        }
        dLink = dLink :+ Link(elem.compTerm, elem.baseTerm, (elem.distVal * 5).toInt)
      }
    })

    dNodes.foreach(println)



//    setTerm.foreach(println)
//    nodes.foreach(println)
//    var i = 0
//    val lstNodes = nodes.map(strNode => {
//      Node(strNode, {(i += 1); i})
//    })




    WordLink(dNodes, dLink)
  }

  get("/news/termdist1") {
    implicit val getTermDistResult = GetResult(r => TermDist(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))
    db.run(sql"select * from TERM_DIST".as[TermDist])
  }

  case class WordLink(nodes: Array[Node], links: Array[Link])
  case class Node(id: String, group: Int)
  case class Link(source: String, target: String, value: Int)
}

class NewsJsController(val db: Database) extends ScalatraServlet with FutureSupport with NewsDataProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}