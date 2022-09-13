package com.yg.data

import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._

import java.sql.Timestamp
import java.time.LocalDateTime

object DtRepo {
  case class LdaTopic(
                       ldaNo: Option[Int],
                       topicNo: Int,
                       term: String,
                       score: Double,
                       startMinAgo: Int,
                       seedNo: Int,
                       grpTs: Long
                     )

  class DtLdaTopicsBinding(tag: Tag) extends Table[LdaTopic](tag, "DT_LDA_TOPICS") {
    def ldaNo = column[Int]("LDA_NO", O.PrimaryKey, O.AutoInc)

    def topicNo = column[Int]("TOPIC_NO")

    def term = column[String]("TERM")

    def score = column[Double]("SCORE")

    def startMinAgo = column[Int]("START_MIN_AGO")

    def seedNo = column[Int]("SEED_NO")

    def grpTs = column[Long]("GRP_TS")

    def * = (ldaNo.?, topicNo, term, score, startMinAgo, seedNo, grpTs) <>
      (LdaTopic.tupled, LdaTopic.unapply)
  }

  case class TermDist(
                       termNo: Option[Int],
                       baseTerm: String,
                       compTerm: String,
                       distVal: Double,
                       tRangeMinAgo: Int,
                       seedNo: Int,
                       grpGs: Long
                     )

  class TermDistBinding(tag: Tag) extends Table[TermDist](tag, "TERM_DIST") {
    def termNo = column[Int]("TERM_NO", O.PrimaryKey, O.AutoInc)

    def baseTerm = column[String]("BASE_TERM")

    def compTerm = column[String]("COMP_TERM")

    def distVal = column[Double]("DIST_VAL")

    def tRangeMinAgo = column[Int]("T_RANGE_MIN_AGO")

    def seedNo = column[Int]("SEED_NO")

    def grpTs = column[Long]("GRP_TS")

    def * = (termNo.?, baseTerm, compTerm, distVal, tRangeMinAgo, seedNo, grpTs) <>
      (TermDist.tupled, TermDist.unapply)
  }

  val ldaTopicTable = TableQuery[DtLdaTopicsBinding]
  val latestGprTs = ldaTopicTable.map(_.grpTs).max

  def latestAllTopics = ldaTopicTable.filter(_.grpTs === latestGprTs).sortBy(_.topicNo)

  // ----
  val termDistTable = TableQuery[TermDistBinding]
  val latestTdGrpTs = termDistTable.map(_.grpTs).max
  val latestTdGrpTs1 = termDistTable.sortBy(_.termNo.desc).take(1).result

  def termDists(grpTs: Long) = termDistTable.filter(_.grpTs === latestTdGrpTs).sortBy(_.baseTerm)

  def getDistBaseTerm(grpTs: Long) = termDists(grpTs).groupBy(_.baseTerm).map { case (baseTerm, group) => baseTerm }

  def getCompTerms(grpTs: Long, baseTerm: String, limit: Int) = termDists(grpTs).filter(_.baseTerm === baseTerm)
    .sortBy(_.distVal.desc).take(limit).result

  implicit val getTermDistResult = GetResult(r => TermDist(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

//  def getCompTermsFast =
//    sql"""select * from TERM_DIST""".as[Seq[TermDist]]

}