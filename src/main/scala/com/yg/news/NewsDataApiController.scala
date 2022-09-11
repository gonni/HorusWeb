package com.yg.news

import com.yg.conn.InfluxClient
import com.yg.data.DtRepo
import com.yg.data.DtRepo.LdaTopic
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.slf4j.LoggerFactory

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import slick.jdbc.MySQLProfile.api._

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}

trait NewsWebDataProcessing extends ScalatraServlet with FutureSupport {
  val logger =  LoggerFactory.getLogger(getClass)
  val tsPattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  def db: Database

  get("/api/termCount") {
    val term = params("term").trim
    val start = params("start").toLong
    val stop = params("stop").toLong
    val step = params("step").trim

    val tsStart = tsPattern.format(new Date(start))
    val tsStop = tsPattern.format(new Date(stop))

    logger.info(s"start/stop = ${tsStart}/${tsStop}")
    logger.info(s"Request Statistics for ${term}/${start}/${stop}/${step}")

    val res = InfluxClient.getTermCount(term, (start/1000).toString, (stop/1000).toString, "1m")
    logger.info(term + " count size : " + res.size)
    val csvData = res.map(dpc => dpc.ts + "," + dpc.count).mkString("\n")
    val response = "Date,Hits\n" + csvData
    response
  }

  get("/api/topic") {
    logger.info("detected topic data")
    val res : Future[Seq[LdaTopic]] = db.run(DtRepo.latestAllTopics.result)
    val topics = Await.result(res, Duration.Inf)
//    println(topics)

    val sb = new mutable.StringBuilder("flare,\n")
    var tempTopicNo = -1
    for(e <- topics) {
      if(e.topicNo != tempTopicNo) {
        tempTopicNo = e.topicNo
        sb.append("flare.topic" + e.topicNo + ",\n")
      }
      sb.append("flare.topic" + e.topicNo + "." + e.term + "," + (e.score * 10000).toInt + "\n")
    }

    println("Body->\n" + sb)
    "id,value\n" + sb
  }


}

class NewsDataApiController(val db: Database) extends ScalatraServlet with FutureSupport with NewsWebDataProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
