package com.yg.processing

import com.yg.RuntimeConfig
import com.yg.data.{DtRepo, TermScoreRepo}
import com.yg.news.Node
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

  def topicTermDicsWithScore(seedNo: Int, limit: Int, topN: Int) = {
    topicTermDics(seedNo, limit).map(v => {
      (v.map(_._1.term).mkString("|"), v.map(_._1.score).reduce((a1, a2) => a1 + a2))
    }).sortBy(-_._2).take(topN)
  }

  def topicTermDics(seedNo: Int, limit: Int) = {

    val stopWords = loadStopWords(-10)

//    val asynLdaResult = db.run(DtRepo.latestAllTopics.filter(_.seedNo === seedNo).result)

    val latestTs = Await.result(db.run(DtRepo.latestSeedGprTs(seedNo).result), 10.seconds)
    println(s"lts for ${seedNo} :" + latestTs)

    val asynLdaResult = db.run(DtRepo.latestSeedTopics(latestTs.get).result)
    val topics = Await.result(asynLdaResult, 10.seconds)

    val topicNos = topics.map(_.topicNo).distinct

    topicNos.map(topicNo => {
//      println(s"=======${topicNo}========")
      topics
        .filter(_.topicNo == topicNo)
        .filter(topic => !stopWords.contains(topic.term))
        .sortBy(- _.score)
        .zipWithIndex.filter{ case(topic, index) =>  index < limit }
    })
  }

  def integratedTermGraph(targetSeeds: Seq[Int], limit: Int) = {
    targetSeeds.map(seedNo => {
      val resData = getScoredTc(seedNo, 0.003)
      (seedNo, resData.take(limit))
    })
  }

  def mergedTermGraph(targetSeeds: Seq[Int], termsPerTopic: Int, topicsPerSeed: Int) = {
    integratedTermGraph(targetSeeds, topicsPerSeed).map(r => (r._1, r._2.map(_.take(termsPerTopic))))
  }

  def mergedTermGraphBoosted(targetSeeds: Seq[SeedBoost]) = {
//    integratedTermGraph(targetSeeds, topicsPerSeed).map(r => (r._1, r._2.map(_.take(termsPerTopic))))
    targetSeeds.map(sb => {
      val resData = getScoredTc(sb.seedId, 0.003, sb.boost)
      (sb.seedId, resData.take(sb.topicCnt))
//    }).map(r => (r._1, r._2.map(_.take(termsPerTopic))))
    }).map(r => (r._1, r._2.map(_.take(targetSeeds.filter(a => a.seedId == r._1)
      .headOption.getOrElse(SeedBoost(0,0,3,0)).maxTermsPerTopic))))
  }

  /**
   * grpScore x listOrderScore x TF (cleaned with stopWords) x Future(TF by Influx)
   */
  def getScoredTc(seedNo: Int, minScore: Double = 0.03, seedBoost: Double = 1.0) = {

    // (String, Int) Order by value desc
    val mapTermCount = getOrderedTc(seedNo, minScore)(i => i.baseTerms ++ i.relTerms).toMap[String, Int]

    val ldaData = getLdaTdmSummaryData(seedNo)
    ldaData.map(ddu => {
      val grpScore = ddu.topicScore
//      println("->" + ddu)

      val baseSize = ddu.baseTerms.length
      ddu.baseTerms.zipWithIndex.map{ case(elem, index) =>
//        println(elem, index)
//        println(elem, ((baseSize - index) / baseSize), grpScore)

        val tcBoosting = if(mapTermCount.contains(elem)) 1 + 0.1 * mapTermCount(elem)  else 1
//        if(boosting > 1) println("Upper Boost : " + elem + "->" + boosting)
        (elem, ((baseSize - index) * 10 / baseSize) + 30 * grpScore * tcBoosting * seedBoost / 2)

      }
    })
  }

  def getOrderedTc[B](seedNo: Int, minTopicScore: Double)(targetElement: DtmData => IterableOnce[B]) = {
    getLdaTdmSummaryData(seedNo)
      .filter(_.topicScore > minTopicScore)
      .flatMap(targetElement)
      .groupBy(i=>i).map(e => (e._1, e._2.length))
      .toList.sortBy(_._2)(Ordering[Int].reverse)
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

  // DtmData
  def getLdaTdmSummaryData(seedNo: Int, cleanLevel : Int = 1) = {
    val stopWordsSet = loadStopWords(cleanLevel)

    val grpTs = Await.result(db.run(DtRepo.getLatesetTtdmGrp(seedNo)), 10.seconds)
//    println("Latest GrpTs =" + grpTs)
    val dataBody = Await.result(db.run(DtRepo.getTtdm(grpTs.headOption.get._6)), 10.seconds)

    dataBody.map(row => DtmData(
      row._2.split("\\|").toList.filter(word => !stopWordsSet.contains(word)),
      row._3.split("\\|").toList.filter(word => !stopWordsSet.contains(word)), row._4))

  }
}

case class SeedBoost(seedId: Int, topicCnt: Int, maxTermsPerTopic:Int, boost: Double)
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
//    ta.topicTermDics(21, 7).map(v => {
//      (v.map(_._1.term).mkString("|"), v.map(_._1.score).reduce((a1,a2) => a1 + a2))
//    }).foreach(println)
//
//    println("--------------------------------------")
//    ta.topicTermDicsWithScore(21, 7, 3).foreach(println)

//    ta.mergedTermGraph(Seq(21,23,25), 3, 3).foreach(println)
    println("--------------------------------------")
    ta.mergedTermGraphBoosted(
      Seq(SeedBoost(21,5,3,1.7),
        SeedBoost(23,1,2,0.5),
        SeedBoost(25,1,3,0.3)))
      .foreach(println)


//    ta.mergedTermGraph(Seq(21), 3, 2).foreach(println)
//    val md = ta.integratedTermGraph(Seq(21), 3)
//    md.map(a => {
//      a._2.foreach(println)
//    })

//    def integratedTermGraph(targetSeeds: Seq[Int], limit: Int) = {
//      targetSeeds.map(seedNo => {
//        val resData = ta.getScoredTc(seedNo, 0.003)
//        (seedNo, resData.take(limit))
//      })
//    }
//
//    integratedTermGraph(Seq(21), 2).map(a => {
//      a._2.map(_.take(3)).foreach(println)
//    })

//    var nodes = md.flatMap(idLst => {
//      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
//        termScore.zipWithIndex.map { case (a, i) =>
//          Node(a._1, (idLst._1 * 100 + ig), a._2.toInt)
//        } // List
//      }
//    }).flatMap(i => i).toArray
//
//
//    var links = md.flatMap(idLst => {
//      nodes = nodes :+ Node("Seed#" + idLst._1, 100)
//
//      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
//        nodes = nodes :+ Node("T#" + ig, 50)
//        ("T#" + ig, "Seed#" + idLst._1, 10)
//
//        termScore.zipWithIndex.map { case (a, i) =>
////          (a._1, a._2, idLst._1, ig ,i)
//          (a._1, "T#" + ig, a._2.toInt)
//        } // List
//      }
//    }).flatMap(i => i)
//
//
//    nodes.foreach(println)
//    println("-----------------------------")
//
//    links.foreach(println)

//    ta.getLdaTdmSummaryData(21).foreach(println)
//
//    val a = ta.getOrderedTc(21, 0.001)(i => i.baseTerms ++ i.relTerms).take(10)
//    a.foreach(println)
//
//    ta.getLdaTdmSummaryData(23).foreach(println)
//    ta.getOrderedTc(23, 0.001)(i => i.baseTerms ++ i.relTerms).take(10).foreach(println)
//
//    ta.getLdaTdmSummaryData(25).foreach(println)
//    ta.getOrderedTc(25, 0.001)(i => i.baseTerms ++ i.relTerms).take(10).foreach(println)

//    ta.getScoredTc(1).foreach(println)

//    val x = ta.integratedTermGraph(Seq[Int](21, 23), 3)//.foreach(println)
//    val x2= x.flatMap(idLst => {
//      idLst._2.flatMap(termScore => { // Vector
//        termScore.zipWithIndex.map{ case(a, i) => {
//          (a._1, a._2, idLst._1, i.toInt)
//        }}  // List
//      })
//    })
//
//    x2.foreach(println)
//    println("-----------------------------")
//
//    val x3 = x.flatMap(idLst => {
//      idLst._2.zipWithIndex.map { case(termScore, ig) => { // Vector
//        termScore.zipWithIndex.map { case (a, i) => {
//          (a._1, a._2, idLst._1, ig, i)
//        }
//        } // List
//      }}
//    })
//
//    x3.flatMap(i=>i).foreach(println)

//    ta.topicTermDics(21, 5).foreach(println)

//    println("-----------------------------")
//    val lstTerms = ta.integratedTermGraph(Seq[Int](1, 2),3)
//      .flatMap(_._2).flatMap(_.map(_._1))
//    println(lstTerms.length + " -- distinct --> " + lstTerms.distinct.length)
  }
}
