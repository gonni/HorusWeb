package com.yg.processing

import slick.jdbc.MySQLProfile.api._
class TermAnalyzer(val db: Database) {

  /**
   * TFIDF x Influxdb Score
   *  - Influxdb : latest peak score by counting tf
   *  - TF-IDF : TFIDF score based on 1 week or 1 month model
   *  - Merge & Boosting score :
   */
  def getTermScore(term: String) = {
    ???
  }

  def getTermGroupScore(terms: Seq[String]) = {
    ???
  }

}

object TermAnalyzer {
  def main(args: Array[String]): Unit = {
    println("Active ..")

  }
}
