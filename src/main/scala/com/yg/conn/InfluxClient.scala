package com.yg.conn

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import com.influxdb.annotations.{Column, Measurement}
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.client.write.Point

import java.time.Instant
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

  def main(args: Array[String]): Unit = {
    println("Active")

    getHighTerms("-6h", 10).foreach(println)

    println("Finished")
    system.terminate()
  }
}
