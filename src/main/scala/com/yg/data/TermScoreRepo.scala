package com.yg.data

import slick.jdbc.MySQLProfile.api._

object TermScoreRepo {
  case class TermScore(
                      tsNo: Option[Int],
                      token: String,
                      avgTfidf: Double,
                      avgDf: Int,
                      dataRangeMin: Int,
                      seedNo: Int,
                      grpTs: Long
                      )

  class TermScoreBinding(tag: Tag) extends Table[TermScore](tag, "DT_TERM_SCORE") {
    def tsNo = column[Int]("TS_NO", O.PrimaryKey, O.AutoInc)
    def token = column[String]("TOKEN")
    def avgTfidf = column[Double]("AVG_TFIDF")
    def avgDf = column[Int]("AVG_DF")
    def dataRangeMin = column[Int]("DATA_RANGE_MIN")
    def seedNo = column[Int]("SEED_NO")
    def grpTs = column[Long]("GRP_TS")

    def * = (tsNo.?, token, avgTfidf, avgDf, dataRangeMin, seedNo, grpTs) <>
      (TermScore.tupled, TermScore.unapply)
  }

  val termScoreTable = TableQuery[TermScoreBinding]



}
