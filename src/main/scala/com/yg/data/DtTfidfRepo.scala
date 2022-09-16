package com.yg.data

import slick.jdbc.MySQLProfile.api._

object DtTfidfRepo {
  case class TfIdf(
                  tfidfNo: Option[Int],
                  token: String,
                  docId: Int,
                  tf: Int,
                  df: Int,
                  idf: Double,
                  tfidf: Double,
                  startMinAgo: Int,
                  seedNo: Int,
                  grpTs: Long
                  )

  class TfidfBinding(tag: Tag) extends Table[TfIdf](tag, "DT_TFIDF") {
    def tfidfNo = column[Int]("TFIDF_NO", O.PrimaryKey, O.AutoInc)
    def token = column[String]("TOKEN")
    def docId = column[Int]("DOC_ID")
    def tf = column[Int]("TF")
    def df = column[Int]("DF")
    def idf = column[Double]("IDF")
    def tfidf = column[Double]("TFIDF")
    def startMinAgo = column[Int]("START_MIN_AGO")
    def seedNo = column[Int]("SEED_NO")
    def grpTs = column[Long]("GRP_TS")

    def * = (tfidfNo.?, token, docId, tf, df, idf, tfidf, startMinAgo, seedNo, grpTs) <>
      (TfIdf.tupled, TfIdf.unapply)
  }

  val tfIdfTable = TableQuery[TfidfBinding]
}
