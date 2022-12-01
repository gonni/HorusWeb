package com.yg.processing

import com.yg.conn.InfluxClient
import com.yg.conn.InfluxClient.TermCount
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.scalatra.forms.FormSupport
import org.scalatra.i18n.I18nSupport
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.MySQLProfile.api._

trait StgStreamingShowProcessing extends ScalatraServlet {
  val logger: Logger
  def db: Database

  get("/termCloud") {
    val seedNo = params("seedNo").toInt
    com.yg.news.html.wordCloud.render(seedNo)
  }

  get("/termCloud2") {
    val seedNo = params("seedNo").toInt
    com.yg.news.html.wordCloud2.render(seedNo)
  }

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

  get("/topicCell") {
    com.yg.news.html.newsTopic.render()
  }

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

    com.yg.news.html.streamShow.render(targetTerms.map(t => TermCount(t, 0)))
  }
}

trait LiveStreamingShowProcessing extends ScalatraServlet
  with FormSupport with I18nSupport with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)
  def db: Database

  get("/topic3d") {
    logger.info("topic3d ..")
//    com.yg.news.html.multiWordLink.render()
    com.yg.live.html.link3d.render()
  }

  // map to tcStreamDark.html
  get("/multiTopicMon") {
    val seedNo = params("seedNo").toInt
    val clientIp = request.getRemoteAddr
    logger.info(s"Requested recommended news ..${clientIp}")

    val ta = new TopicAnalyzer(db)
    val targetTerms = ta.topicTermDics(seedNo, 7).map(topic => {
      topic.map(lda => {
        lda._1.term
      }).mkString("|")
    })

    com.yg.live.html.tcStreamDark.render(targetTerms.map(t => TermCount(t, 0)))
  }

  // map to t1div2updown.html
  get("/m") {
    logger.info("Detected Main page ..")
//    layouts.html.dashboard.render("Horus :: RT Topic", com.yg.live.html.liveMain.render())
    layouts.html.dashboard.render("Horus :: RT Topic", com.yg.live.html.t1div2updown.render())
  }

  // map to t1div2updown.html
  get("/mv") {
    logger.info("Detected Main page ..")

    layouts.html.dashboard.render("Horus :: RT Topic", com.yg.live.html.tpDiv2EastWest.render())
  }
}

class LiveStreamingShowController(val db: Database) extends ScalatraServlet
  with FutureSupport with LiveStreamingShowProcessing with StgStreamingShowProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
