package com.yg.news

import com.yg.RuntimeConfig
import com.yg.processing.TopicAnalyzer
import org.scalatra.{FutureSupport, ScalatraServlet}
import slick.jdbc.MySQLProfile.api._

trait AdvancedDataProcessing extends ScalatraServlet {
  def db: Database

  get("/js/circleTopic3d") {
    val ta = new TopicAnalyzer(db)
    val md = ta.integratedTermGraph(Seq(1), 3)

    val nodes = List(
      Node("A", 10, 10),
      Node("B", 10, 10),
      Node("C", 10, 10),
      Node("D", 10, 10),
      Node("E", 10, 10),
      Node("F", 10, 10),
    ).toArray

    val links = List(
      Link("A", "B", 10),
      Link("B", "C", 10),
      Link("C", "D", 10),
      Link("D", "E", 10),
      Link("E", "F", 10),
      Link("F", "A", 10),
    ).toArray

    WordLink(nodes, links)
  }

  get("/js/v1/topic3d") {
    val ta = new TopicAnalyzer(db)
    val md = ta.integratedTermGraphEx(Seq(1,2), 10)

    val termCnt = md.flatMap(idLst => {
      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
        termScore.zipWithIndex.map { case (a, i) =>
          Node(a._1, (idLst._1 * 100 + ig), a._2.toInt)
        }
      }
    }).flatMap(i => i).groupBy(_.id).view.mapValues(_.size)

    termCnt.foreach(println)
//    md.filter(r => r.)



  }

  // ALL NEW
  get("/js/multiSeedsTopic3d") {
    println("Detected Multi ")
    val ta = new TopicAnalyzer(db)
    val md = ta.integratedTermGraph(Seq(1,2), 3)

    var nodes = md.flatMap(idLst => {
      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
        termScore.zipWithIndex.map { case (a, i) =>
          Node(a._1, (idLst._1 * 100 + ig), a._2.toInt)
        }
      }
    }).flatMap(i => i).toArray.distinctBy(_.id)

    nodes.foreach(println)

    var links = md.flatMap(idList => {
      nodes = nodes :+ Node("Seed#" + idList._1, 9999, 3)
      idList._2.zipWithIndex.map { case (termScore, ig) => // Vector
        nodes = nodes :+ Node("T#" + idList._1 + ":" + ig, ig * 1000 ,3)
        termScore.zipWithIndex.map { case (a, i) =>
          Link(a._1, "T#" + idList._1 + ":" + ig, a._2.toInt)
        } // List
      }
    }).flatMap(i => i).toArray

    md.flatMap(idList => {
//      links = links :+ Link()
      idList._2.zipWithIndex.map { case (termScore, ig) => {
        links = links :+ Link("T#" + idList._1 + ":" + ig, "Seed#" + idList._1, 10)  // Topic# --> Seed#
      }
      }
    })

    WordLink(nodes, links)
  }

}

object DevMain {
  def main(args: Array[String]): Unit = {
    println("Active Dev ..")

    val db = Database.forURL(url = RuntimeConfig("mysql.url"),
      user = RuntimeConfig("mysql.user"),
      password = RuntimeConfig("mysql.password"),
      driver = "com.mysql.cj.jdbc.Driver")

    val ta = new TopicAnalyzer(db)
    val md = ta.integratedTermGraph(Seq(1,2), 10)

    val termCnt = md.flatMap(idLst => {
      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
        termScore.zipWithIndex.map { case (a, i) =>
          Node(a._1, (idLst._1 * 100 + ig), a._2.toInt)
        }
      }
    }).flatMap(i => i).groupBy(_.id).view.mapValues(_.size)

    termCnt.foreach(println)

    md.foreach(println)
    println("--------------------")
    md.map(r => (r._1, r._2.take(1))).foreach(println)


    //    md.filter(r => r.)

  }


}

