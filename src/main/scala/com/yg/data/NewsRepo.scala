package com.yg.data

import slick.jdbc.MySQLProfile.api._
import java.sql.Timestamp

object NewsRepo {

  case class NewsClick(clickId: Int, userId: String, newsId: Int, clickDt: Timestamp)

  class NewsClickBinding(tag: Tag) extends Table[NewsClick](tag, "NEWS_CLICK") {
    def clickId = column[Int]("CLICK_ID", O.PrimaryKey, O.AutoInc)
    def userId = column[String]("USER_ID")
    def newsId = column[Int]("NEWS_ID")
    def clickDt = column[Timestamp]("CLICK_DT")

    def * = (clickId, userId, newsId, clickDt) <> (NewsClick.tupled, NewsClick.unapply)
  }

  val newsClickTable = TableQuery[NewsClickBinding]

  val insertClickLog = (nc: Seq[NewsClick]) => {
    newsClickTable ++= nc
  }

}
