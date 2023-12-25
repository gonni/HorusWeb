package com.yg.ais.nlp

import com.yg.RuntimeConfig
import com.yg.data.DtRepo
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import scala.jdk.CollectionConverters._

//import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.collection.IterableOnce._
import scala.collection.Map
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TopicTermAnalyzer (val db: Database) {
  val logger = LoggerFactory.getLogger(getClass)
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

  val tdm: Map[String, Map[String, Double]] = Await.result(loadTdm(), Duration.Inf)
  val komoran =  new Komoran(DEFAULT_MODEL.LIGHT)

  private def loadTdm() = {
    for {
//      latestTs <- db.run(DtRepo.TermDistFunction.getLatestGrpTs(21))
//      data <- db.run(DtRepo.TermDistFunction.getTermDist(latestTs.head.grpGs))
      latestTs <- db.run(DtRepo.CommonTermDist.ApiFunction.getLatestGrpTs(21))
      data <- db.run(DtRepo.CommonTermDist.ApiFunction.getTermDist(latestTs.head.grpGs))
    } yield data
      .groupBy(_.baseTerm)
      .map {
        case (key, value) => {
          val mapped = value.map(v => {
            (v.compTerm, v.distVal)
          }).toMap[String, Double]
          (key, mapped)
        }
      }.toMap[String, Map[String, Double]]
  }

  def allTopicScore(sentence: String): Map[String, Double] = {
    tdm.keySet.map(key => {
      (key, getTopicScore1(key, sentence).getOrElse(0.0))
    }).toMap
  }

  private def getTopicScore(topic: String, sentence: String) = {
    val tokens = komoran.analyze(sentence).getTokenList.asScala.map(_.getMorph).toList
//    val termScore: Option[Map[String, Double]] = tdm.get(topic)

    for {
      termScoreMap <- tdm.get(topic)
    } yield tokens.map(token => {
      termScoreMap.getOrElse(token, 0.0)
    }).sum
  }

  private def getTopicScore1(topic: String, sentence: String) = {
    val tokens = komoran.analyze(sentence).getTokenList.asScala.map(_.getMorph).toList
    //    val termScore: Option[Map[String, Double]] = tdm.get(topic)

    for {
      termScoreMap <- tdm.get(topic)
    } yield {
      val tm = termScoreMap + (topic -> 1.0)
      tokens.map(token => {
        tm.getOrElse(token, 0.0)
      }).sum
    }
  }

}

object TopicTermAnalyzer {
  def main(args: Array[String]): Unit = {
    println("Active System ..")

//    val test = new TopicAnalyzer(db)
//    test.allTopicScore("한국 보증 화재 투입 대응 작가 채권은 주택 가격 하락을 불렀다.").foreach(println)

//    val result = Await.result(test.loadTdm(), Duration.Inf)
//    result.foreach(row => {
//      println(row)
//    })
//    println("result length ->" + result.length)
//
//    val res1 = result.groupBy(_.baseTerm)
//    println("--->" + res1)
//
//    val mapp = res1.map{
//      case (key, value) => {
//        val mapped = value.map(v => {
//          (v.compTerm, v.distVal)
//        }).toMap[String, Double]
//        (key, mapped)
//      }
//    }.toMap[String, Map[String, Double]]
//
//    mapp.foreach(println)

    println("Completed ..")
  }
}

