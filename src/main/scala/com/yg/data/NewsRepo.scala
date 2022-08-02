package com.yg.data

import slick.jdbc.MySQLProfile.api._

import java.sql.Timestamp
import java.time.LocalDateTime

object NewsRepo {

  case class NewsClick(clickId: Option[Int] = None,
                       userId: String = "local",
                       newsId: Int,
                       pageCd: String = "NA",
                       clickDt: Timestamp = Timestamp.valueOf(LocalDateTime.now()))

  class NewsClickBinding(tag: Tag) extends Table[NewsClick](tag, "NEWS_CLICK") {
    def clickId = column[Int]("CLICK_ID", O.PrimaryKey, O.AutoInc)
    def userId = column[String]("USER_ID")
    def newsId = column[Int]("NEWS_ID")
    def pageCd = column[String]("PAGE_CD")
    def clickDt = column[Timestamp]("CLICK_DT")

    def * = (clickId.?, userId, newsId, pageCd, clickDt) <> (NewsClick.tupled, NewsClick.unapply)
  }

  val newsClickTable = TableQuery[NewsClickBinding]

  val insertClickLog = (nc: Seq[NewsClick]) => {
    newsClickTable ++= nc
  }

}
