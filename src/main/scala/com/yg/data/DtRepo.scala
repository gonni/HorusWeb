package com.yg.data

import slick.jdbc.MySQLProfile.api._

object DtRepo {

  class DtTopicTdm(tag: Tag)
    extends Table[(Int, String, String, Double, Int, Long)](tag, "DT_TOPIC_TDM") {
    def ttNo = column[Int]("TT_NO", O.PrimaryKey, O.AutoInc)
    def baseTerm = column[String]("BASE_TERM")
    def nearTerm = column[String]("NEAR_TERM")
    def topicScore = column[Double]("TOPIC_SCORE")
    def seedNo = column[Int]("SEED_NO")
    def grpTs = column[Long]("GRP_TS")

    def * = (ttNo, baseTerm, nearTerm, topicScore, seedNo, grpTs)
  }

  val dtTopicTdmTable = TableQuery[DtTopicTdm]

  def getLatesetTtdmGrp(seedNo: Int) = {
    dtTopicTdmTable
      .filter(_.seedNo === seedNo)
      .sortBy(_.ttNo.desc)
      .take(1)
      .result
  }

  def getTtdm(grpTs: Long) = {
    dtTopicTdmTable
      .filter(_.grpTs === grpTs)
      .sortBy(_.topicScore.desc)
      .result
  }

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

  // ----- Functions for LDA -----
  def latestAllTopics = ldaTopicTable.filter(_.grpTs === latestGprTs).sortBy(_.topicNo)

  def latestSeedGprTs(seedNo: Int) = ldaTopicTable.filter(_.seedNo === seedNo).map(_.grpTs).max
  def latestSeedTopics(maxGrp: Long) = ldaTopicTable.
    filter(_.grpTs === maxGrp)
    .sortBy(_.topicNo)

  // ----- Functions for TDM -----
  val termDistTable = TableQuery[TermDistBinding]
  val latestTdGrpTs = termDistTable.map(_.grpTs).max
//  val latestTdGrpTs1 = termDistTable.sortBy(_.termNo.desc).map(_.grpTs).take(1).result.headOption
  val latestTdGrpTs1 = termDistTable.sortBy(_.termNo.desc).take(1).result

  def termDists(grpTs: Long) = termDistTable.filter(_.grpTs === grpTs).sortBy(_.baseTerm)

//  def allLatestTerms(seedNo: Int) = termDistTable.filter(_.grpTs === latestTdGrpTs1)

  def getDistBaseTerm(grpTs: Long) = termDists(grpTs).groupBy(_.baseTerm).map { case (baseTerm, group) => baseTerm }

  def getCompTerms(grpTs: Long, baseTerm: String, limit: Int) = termDists(grpTs).filter(_.baseTerm === baseTerm)
    .sortBy(_.distVal.desc).take(limit).result



  def getCompTermsFast(grpTs: Long) = {
//    implicit val getTermDistResult = GetResult(r => TermDist(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

    sql"""
        select a.BASE_TERM, a.COMP_TERM, a.DIST_VAL,
        ( select 1 + count(*) from (select * from TERM_DIST where GRP_TS = $grpTs) a1
        where a1.BASE_TERM = a.BASE_TERM and a1.DIST_VAL > a.DIST_VAL) as RNK
        from (select * from TERM_DIST where GRP_TS = $grpTs) a ;
       """.as[(String, String, Double, Int)]
  }

}