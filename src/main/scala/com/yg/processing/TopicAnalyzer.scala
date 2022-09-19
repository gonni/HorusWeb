package com.yg.processing

import com.yg.RuntimeConfig
import com.yg.data.DtRepo
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait TopicProcessing {
  def db: Database

  def topicTermDics(seedNo: Int) = {
    val asynLdaResult = db.run(DtRepo.latestAllTopics.filter(_.seedNo === seedNo).result)
    val res = Await.result(asynLdaResult, 10.seconds)
    res.foreach(println)
    //TODO data convert

  }

  def w2vSimilarTerms(term: String, limit: Int) = {
    val latestGrpVal = Await.result(db.run(DtRepo.latestTdGrpTs1), 10.seconds).get

  }

}


class TopicAnalyzer(val db: Database) extends TopicProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}

case class TopicTerms(topicNo: Int,
                      mainTerm: String,
                      subTerms: Seq[String],
                      relatedTerms: Seq[String])

object TopicAnalyzer {


  def main(args: Array[String]): Unit = {
    println("Active ..")

    val db = Database.forURL(url = RuntimeConfig("mysql.url"),
      user = RuntimeConfig("mysql.user"),
      password = RuntimeConfig("mysql.password"),
      driver = "com.mysql.cj.jdbc.Driver")

    val ta = new TopicAnalyzer(db)
    ta.topicTermDics(1)

  }
}
