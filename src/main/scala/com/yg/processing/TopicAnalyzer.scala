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
    //TODO
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
      val baseTerms = getOrderedTc(seedNo, 0.03)(_.baseTerms)
      baseTerms.foreach(println)



    })
  }

  def getTopCountTerms(seeds: Seq[Int]) = {
    val topTerms = seeds.map(seed => {
      getOrderedTc(seed, 0.001)(i => i.baseTerms ++ i.relTerms).filter(_._2 > 1).map(_._1)
    })
    topTerms.foreach(println)

    topTerms.flatMap(_.toSeq)
      .groupBy(i=>i).map(e=> (e._1, e._2.length))
      .toList.sortBy(_._2)(Ordering[Int].reverse)
      .foreach(println)
  }

  def getOrderedTc[B](seedNo: Int, minTopicScore: Double)(fm: DtmData => IterableOnce[B]) = {
    getLdaTdmSummaryData(seedNo)
      .filter(_.topicScore > minTopicScore)
      .flatMap(fm)
      .groupBy(i=>i).map(e => (e._1, e._2.length))
      .toList.sortBy(_._2)(Ordering[Int].reverse)
  }

  // DtmData
  def getLdaTdmSummaryData(seedNo: Int, cleanLevel : Int = 1) = {
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

    println("-----------------------------")
    ta.getLdaTdmSummaryData(21).foreach(println)

    val a = ta.getOrderedTc(21, 0.001)(i => i.baseTerms ++ i.relTerms).take(10)
    a.foreach(println)

    ta.getLdaTdmSummaryData(23).foreach(println)
    ta.getOrderedTc(23, 0.001)(i => i.baseTerms ++ i.relTerms).take(10).foreach(println)

    ta.getLdaTdmSummaryData(25).foreach(println)
    ta.getOrderedTc(25, 0.001)(i => i.baseTerms ++ i.relTerms).take(10).foreach(println)

  }
}
