package com.yg.processing

import com.yg.data.DtRepo
import slick.jdbc.MySQLProfile.api._

trait TopicProcessing {
  def db: Database

  def topicTermDics(seedNo: Int) = {
    val resLda = db.run(DtRepo.latestAllTopics.filter(_.seedNo === seedNo).result)
    resLda

//    db.run(DtRepo.termDistTable.)

  }

}


class TopicAnalyzer(val seedNo: Long, val db: Database) extends TopicProcessing {

}

case class TopicTerms()

object TopicAnalyzer {


  def main(args: Array[String]): Unit = {
    println("Active ..")



  }
}
