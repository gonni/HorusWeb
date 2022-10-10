package com.yg.processing

import com.yg.RuntimeConfig
import com.yg.data.{DtRepo, TermScoreRepo}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait TopicProcessing {
  def db: Database

  def loadStopWords(minScore: Double) = {
    val res = db.run(TermScoreRepo.termScoreTable.filter(_.avgTfidf < minScore).map(_.token).result)
    val stopwords = Await.result(res, 10.seconds)
    stopwords.toSet
  }

  def topicTermDics(seedNo: Int, limit: Int) = {

    val stopWords = loadStopWords(1)

    val asynLdaResult = db.run(DtRepo.latestAllTopics.filter(_.seedNo === seedNo).result)
    val topics = Await.result(asynLdaResult, 10.seconds)
//    topics.foreach(println)
    //TODO data convert
    println("--------------")

    val topicNos = topics.map(_.topicNo).distinct

    topicNos.map(topicNo => {
//      println(s"=======${topicNo}========")
      topics.filter(_.topicNo == topicNo)
        .filter(topic => !stopWords.contains(topic.term))
        .sortBy(- _.score)
        .zipWithIndex.filter{case(topic, index) => {
        index < limit
      }}
    })
  }

  def w2vSimilarTerms(term: String, limit: Int) = {
//    val latestGrpVal = Await.result(db.run(DtRepo.latestTdGrpTs1), 10.seconds).get

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
    ta.loadStopWords(1).foreach(println)
    println("======================================")
    ta.topicTermDics(1, 10).map(topic => {
      topic.map(lda => {
        lda._1.term
      }).mkString("|")
    }).foreach(println)

  }
}
