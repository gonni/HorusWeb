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

  def multiTopicTermDics(limit: Int) = {
    //TODOjett
  }

  def topicTermDics(seedNo: Int, limit: Int) = {

    val stopWords = loadStopWords(1)

//    val asynLdaResult = db.run(DtRepo.latestAllTopics.filter(_.seedNo === seedNo).result)

    val latestTs = Await.result(db.run(DtRepo.latestSeedGprTs(seedNo).result), 10.seconds)
    println(s"lts for ${seedNo} :" + latestTs)

    val asynLdaResult = db.run(DtRepo.latestSeedTopics(latestTs.get).result)
    val topics = Await.result(asynLdaResult, 10.seconds)
    topics.foreach(println)
    //TODO data convert
    println("--------------")

    val topicNos = topics.map(_.topicNo).distinct

    topicNos.map(topicNo => {
//      println(s"=======${topicNo}========")
      topics
        .filter(_.topicNo == topicNo)
        .filter(topic => !stopWords.contains(topic.term))
        .sortBy(- _.score)
        .zipWithIndex.filter{case(topic, index) => {
        println(s"index:limit = ${index}:${limit}" )
        index < limit
      }}
    })
  }

  def integratedTermGraph(targetSeeds: Seq[Int]) = {
    targetSeeds.foreach(seedNo => {
      val baseTerms = getOrderedTermCount(seedNo, 0.5)
      baseTerms.foreach(println)



    })
  }

  def getOrderedTermCount(seedNo: Int, minTopicScore: Double) = {
    getRelationgData(seedNo)
      .filter(_.topicScore > minTopicScore)
      .flatMap(_.baseTerms)
      .groupBy(i=>i).map(e => (e._1, e._2.length))
      .toList.sortBy(_._2)(Ordering[Int].reverse)
  }

  def getRelationgData(seedNo: Int, cleanLevel : Int = 1) = {
    val stopWordsSet = loadStopWords(cleanLevel)

    val grpTs = Await.result(db.run(DtRepo.getLatesetTtdmGrp(seedNo)), 10.seconds)
    println("Latest GrpTs =" + grpTs)
    val dataBody = Await.result(db.run(DtRepo.getTtdm(grpTs.headOption.get._6)), 10.seconds)

    dataBody.map(row => DtmData(
      row._2.split("\\|").toList.filter(word => !stopWordsSet.contains(word)),
      row._3.split("\\|").toList.filter(word => !stopWordsSet.contains(word)), row._4))

  }


}

case class DtmData(baseTerms: List[String], relTerms: List[String], topicScore: Double)

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
//    ta.loadStopWords(1).foreach(println)
//    println("======================================")
//    ta.topicTermDics(1, 10).map(topic => {
//      topic.map(lda => {
//        lda._1.term
//      }).mkString("|")
//    }).foreach(println)

    ta.getRelationgData(21).foreach(println)
    println("-----------------------------")
    println("size =>" + ta.getOrderedTermCount(21, 0.03).size)
    ta.getOrderedTermCount(21, 0.05).foreach(println)
  }
}
