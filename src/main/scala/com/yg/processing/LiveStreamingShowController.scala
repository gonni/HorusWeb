package com.yg.processing

import com.yg.conn.InfluxClient
import com.yg.conn.InfluxClient.TermCount
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.scalatra.forms.FormSupport
import org.scalatra.i18n.I18nSupport
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

trait LiveStreamingShowProcessing extends ScalatraServlet
  with FormSupport with I18nSupport with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/one4all") {
    logger.info("Horus Live Streaming Show ..")

    val highTerms = InfluxClient.getHighTerms("-1h", 30)
    logger.info("highTerms -> " + highTerms.mkString(" | "))

    com.yg.news.html.streamShowMain.render(highTerms)
  }

  get("/show") {
    logger.info("Horus Live Streaming Show on IFrame")

    layouts.html.iStreamShow.render()
  }

  get("/topic3d") {
    logger.info("topic3d ..")
    com.yg.news.html.multiWordLink.render()
  }

  get("/topicCell") {
    com.yg.news.html.newsTopic.render()
  }

  get("/topicMonitor") {
    val seedNo = params("seedNo").toInt
    val clientIp = request.getRemoteAddr
    logger.info(s"Requested recommended news ..${clientIp}")

    val ta = new TopicAnalyzer(db)
    val targetTerms = ta.topicTermDics(seedNo).map(topic => {
      topic.map(lda => {
        lda._1.term
      }).mkString("|")
    })

    //    val mainPage = com.yg.news.html.newsTerm.render(targetTerms.map(t => TermCount(t, 0)))
    //    layouts.html.dashboard.render("NewsStream", mainPage)
    val mainPage = com.yg.news.html.streamShow.render(targetTerms.map(t => TermCount(t, 0)))
//    layouts.html.showbd.render("TopicView", mainPage)
    mainPage
  }

  get("/termCloud") {
    com.yg.news.html.wordCloud.render()
  }

  get("/termCloud2") {
    com.yg.news.html.wordCloud2.render()
  }

}

class LiveStreamingShowController(val db: Database) extends ScalatraServlet
  with FutureSupport with LiveStreamingShowProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
