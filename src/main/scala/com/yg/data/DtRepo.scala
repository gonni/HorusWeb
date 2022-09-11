package com.yg.data

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
    def ldaNo = column[Int]("LDA_NO",  O.PrimaryKey, O.AutoInc)
    def topicNo = column[Int]("TOPIC_NO")
    def term = column[String]("TERM")
    def score = column[Double]("SCORE")
    def startMinAgo = column[Int]("START_MIN_AGO")
    def seedNo = column[Int]("SEED_NO")
    def grpTs = column[Long]("GRP_TS")

    def * = (ldaNo.?, topicNo, term, score, startMinAgo, seedNo, grpTs) <>
      (LdaTopic.tupled, LdaTopic.unapply)
  }

  val ldaTopicTable = TableQuery[DtLdaTopicsBinding]

  val latestGprTs = ldaTopicTable.map(_.grpTs).max

  def latestAllTopics = ldaTopicTable.filter(_.grpTs === latestGprTs).sortBy(_.topicNo)

}
