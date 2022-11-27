package com.yg.news

import com.yg.processing.TopicAnalyzer
import org.scalatra.{FutureSupport, ScalatraServlet}
import slick.jdbc.MySQLProfile.api._

trait AdvancedDataProcessing extends ScalatraServlet {
  def db: Database

//  before() {
//    contentType = formats("json")
//  }

  // ALL NEW
  get("/js/multiSeedsTopic3d") {
    println("Detected Multi ")
    val ta = new TopicAnalyzer(db)
    val md = ta.integratedTermGraph(Seq(21), 3)

    var nodes = md.flatMap(idLst => {
      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
        termScore.zipWithIndex.map { case (a, i) =>
          Node(a._1, (idLst._1 * 100 + ig), a._2.toInt)
        } // List
      }
    }).flatMap(i => i).toArray

    nodes.foreach(println)

    var links = md.flatMap(idLst => {
      nodes = nodes :+ Node("Seed#" + idLst._1, 9999, 3)
      idLst._2.zipWithIndex.map { case (termScore, ig) => // Vector
        nodes = nodes :+ Node("T#" + ig, ig * 1000 ,3)
        termScore.zipWithIndex.map { case (a, i) =>
          Link(a._1, "T#" + ig, a._2.toInt)
        } // List
      }
    }).flatMap(i => i).toArray

    md.flatMap(idList => {
//      links = links :+ Link()
      idList._2.zipWithIndex.map { case (termScore, ig) => {
        links = links :+ Link("T#" + ig, "Seed#" + idList._1, 10)  // Topic# --> Seed#
      }
      }
    })

    WordLink(nodes, links)
  }

}

object DevMain extends AdvancedDataProcessing {
  def main(args: Array[String]): Unit = {

  }

  override def db = ???
}

