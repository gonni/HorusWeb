package com.yg.conn

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import com.influxdb.annotations.{Column, Measurement}
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.client.write.Point

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime}
import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object InfluxClient {
  implicit val system: ActorSystem = ActorSystem("examples")

  val token = "CwgQWYIZKOcSpdlxwpfZfvDWQXpsfTlt7o2GD5hFAs4rTvHDF-7cfwmIQnmdocqL__5uoabCFGuf_GYzFQfxIA=="
  val org = "xwaves"
  val bucket = "tfStudySample"

  val client = InfluxDBClientScalaFactory.create("http://localhost:8086", token.toCharArray, org, bucket)

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
        |  |> filter(fn: (r) => r["seedId"] == "9")
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
//      if(f.getValues == "" || f.getValues == null) println("None Value")
//      println("val => " + f.getValues)
      if(f.getValue != null)
        resData = resData :+ DatePeriodCount(f.getTime.toEpochMilli, f.getValue.toString.toLong)
      else
        resData = resData :+ DatePeriodCount(f.getTime.toEpochMilli, 0L)
    }), Duration.Inf)

    resData
  }

  def main(args: Array[String]): Unit = {
    println("Active")

    getTermCount("비", "-1h", "-1m", "1m").foreach(dpc => {
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