package com.yg.news

import com.yg.conn.InfluxClient
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.slf4j.LoggerFactory

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

trait NewsWebDataProcessing extends ScalatraServlet with FutureSupport {
  val logger =  LoggerFactory.getLogger(getClass)
  val tsPattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

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


}

class NewsDataApiController extends ScalatraServlet with FutureSupport with NewsWebDataProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
