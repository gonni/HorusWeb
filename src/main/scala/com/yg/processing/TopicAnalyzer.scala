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

  def integratedTermGraph(targetSeeds: Seq[Int], limit: Int) = {
    targetSeeds.map(seedNo => {
      val resData = getScoredTc(seedNo)
      (seedNo, resData.take(limit))
    })
  }

//  def integratedTermGraphEx(targetSeeds: Seq[Int], limit: Int) = {
//    targetSeeds.zipWithIndex.map { case(seedNo, i) => {
//      val resData = getScoredTc(seedNo)
//      (seedNo, resData.take(limit))
//    }}
//  }

  /**
   * grpScore x listOrderScore x TF (cleaned with stopWords) x Future(TF by Influx)
   */
  def getScoredTc(seedNo: Int, minScore: Double = 0.03) = {

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

        val boosting = if(mapTermCount.contains(elem)) 1 + 0.3 * mapTermCount(elem)  else 1
//        if(boosting > 1) println("Upper Boost : " + elem + "->" + boosting)
        (elem, ((baseSize - index) * 30 / baseSize) + 10 * grpScore * boosting)

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
    val md = ta.integratedTermGraph(Seq(21, 23), 3)

    var nodes = md.flatMap(idLst => {
      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
        termScore.zipWithIndex.map { case (a, i) =>
          Node(a._1, (idLst._1 * 100 + ig), a._2.toInt)
        } // List
      }
    }).flatMap(i => i).toArray


    var links = md.flatMap(idLst => {
      nodes = nodes :+ Node("Seed#" + idLst._1, 100)

      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
        nodes = nodes :+ Node("T#" + ig, 50)
        ("T#" + ig, "Seed#" + idLst._1, 10)

        termScore.zipWithIndex.map { case (a, i) =>
//          (a._1, a._2, idLst._1, ig ,i)
          (a._1, "T#" + ig, a._2.toInt)
        } // List
      }
    }).flatMap(i => i)

//    md.flatMap(idList => {
//      idList._2.zipWithIndex.map {case(termScore, ig) => {
//        links = links :+ Link()
//      }}
//    })

    nodes.foreach(println)
    println("-----------------------------")

    links.foreach(println)

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
