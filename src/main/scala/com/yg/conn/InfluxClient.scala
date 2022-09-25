package com.yg.conn

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import com.influxdb.annotations.{Column, Measurement}
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.client.write.Point
import com.yg.RuntimeConfig

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime}
import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object InfluxClient {
  implicit val system: ActorSystem = ActorSystem("examples")

//  val token = "lhb_6bgvAirOy1tb4fZ1FtWGR4VbTDDb0QEXCGJsRJcHJmO4RN1SYh4_ob-lnJUOLBKX4-SFFnVkv3H_up6UWQ=="
//  val org = "YG"
//  val bucket = "tfStudySample"
  val url = RuntimeConfig("influx.url")
  val token = RuntimeConfig("influx.authToken")
  val org = RuntimeConfig("influx.org")
  val bucket = "tfStudySample"

  val client = InfluxDBClientScalaFactory.create(url, token.toCharArray, org, bucket)

  case class TermCount(term: String, count: Long)


  /**
   * @param startDt -3h, -10m
   * @param limit
   * @return
   */
  def getHighTerms(startDt : String, limit : Int) = {
    val query =
      s"""
        |from(bucket:"tfStudySample")
        ||> range(start: ${startDt})
        ||> filter(fn: (r) => r._measurement == "term_tf" and r._field == "tf")
        ||> sum()
        |""".stripMargin

    val result = client.getQueryScalaApi().query(query)
    var conv = Seq[TermCount]()
    Await.result(result.runForeach(f => {
      conv = conv :+ TermCount(f.getValueByKey("term").toString, f.getValue.toString.toLong)
    }), Duration.Inf)

    conv.sortBy(-_.count).take(limit)
  }

  case class DatePeriodCount(ts: Long, count: Long)

  /**
   *
   * @param term
   * @param startDt -1h
   * @param endDt -10m
   * @param windowSizeDt 1m
   */
  def getTermCount(term: String, startDt: String, endDt: String, windowSizeDt: String) = {
    val query =
      s"""
        |from(bucket: "tfStudySample")
        |  |> range(start: ${startDt}, stop: ${endDt})
        |  |> filter(fn: (r) => r["_measurement"] == "term_tf")
        |  |> filter(fn: (r) => r["_field"] == "tf")
        |  |> filter(fn: (r) => r["term"] == "${term}")
        |  |> aggregateWindow(every: ${windowSizeDt}, fn: sum, createEmpty: true)
        |  |> yield(name: "sum")
        |""".stripMargin

    val result = client.getQueryScalaApi().query(query)
    var resData = Seq[DatePeriodCount]()
    def date2long(ts: String) = {
      try {
        val localDate = LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        Timestamp.valueOf(localDate).getTime
      } catch {
        case e: Exception => println
      }
      -1L
    }

    Await.result(result.runForeach(f => {
      if(f.getValue != null)
        resData = resData :+ DatePeriodCount(f.getTime.toEpochMilli, f.getValue.toString.toLong)
      else
        resData = resData :+ DatePeriodCount(f.getTime.toEpochMilli, 0L)
    }), Duration.Inf)

    resData
  }

  def getTermsCountSum(terms: Seq[String], startDt: String, endDt: String, windowSizeDt: String) = {

    val qry = terms.map(term => {
      "r[\"term\"] == \"" + term + "\""
    }).mkString(" or ")

    val query =
      s"""
         |from(bucket: "tfStudySample")
         |  |> range(start: ${startDt}, stop: ${endDt})
         |  |> filter(fn: (r) => r["_measurement"] == "term_tf")
         |  |> filter(fn: (r) => r["_field"] == "tf")
         |  |> filter(fn: (r) => (${qry}))
         |  |> aggregateWindow(every: ${windowSizeDt}, fn: sum, createEmpty: true)
         |  |> group(columns: ["_time"], mode:"by")
         |  |> sum(column: "_value")
         |""".stripMargin

    println("full query => " + query)

    val result = client.getQueryScalaApi().query(query)
    var resData = Seq[DatePeriodCount]()

    def date2long(ts: String) = {
      try {
        val localDate = LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        Timestamp.valueOf(localDate).getTime
      } catch {
        case e: Exception => println
      }
      -1L
    }

    Await.result(result.runForeach(f => {
      if (f.getValue != null)
        resData = resData :+ DatePeriodCount(f.getTime.toEpochMilli, f.getValue.toString.toLong)
      else
        resData = resData :+ DatePeriodCount(f.getTime.toEpochMilli, 0L)
    }), Duration.Inf)

    resData
  }



  def main(args: Array[String]): Unit = {
    println("Active")

    val terms = Seq("대통령", "윤", "말")
//
//    val str = terms.map(a => {
//      "r._measurement == " + a
//    }).mkString(" or ")
//    println("mk -> " + str)

    getTermCount("대통령", "-1h", "-1m", "5m").foreach(dpc => {
      println(new Date(dpc.ts) + " --> " + dpc.count)
    })

    println("-------------------")
    getTermsCountSum(terms, "-1h", "-1m", "5m").foreach(dpc => {
      println(new Date(dpc.ts) + " --> " + dpc.count)
    })

//    val strDate = "2022-08-10T02:40:14.1217068Z"
//    val strGen = "2022-08-10T03:00:00Z"
//    val date = LocalDate.parse(strGen, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
//    println("date => " + date)

//    getHighTerms("-6h", 10).foreach(println)
//
//    println("Finished")
    system.terminate()
  }
}
