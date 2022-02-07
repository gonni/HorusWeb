package com.yg.data

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.higherKinds
import scala.util.{Failure, Success}
import slick.basic.DatabasePublisher
import slick.jdbc.MySQLProfile.api._

import java.sql.Date

object HorusSlick {
  class CrawlSeeds(tag: Tag)
    extends Table[(Int, String, String, String)](tag, "CRAWL_SEEDS") {

    def seedNo = column[Int]("SEED_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def urlPattern = column[String]("URL_PATTERN")
    def title = column[String]("TITLE")
    def status = column[String]("STATUS")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (seedNo, urlPattern, title, status)
  }

  class WrapperRules(tag: Tag)
    extends Table[(Int, Int, String, String, String, Date)](tag, "WRAPPER_RULE") {

    def wrapperNo = column[Int]("WRAPPER_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def seedNo = column[Int]("SEED_NO")
    def wrapType = column[String]("WRAP_TYPE")
    def wrapVal = column[String]("WRAP_VAL")
    def wrapName = column[String]("WRAP_NAME")
    def regDt = column[Date]("REG_DT")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (seedNo, seedNo, wrapType, wrapVal, wrapName, regDt)
  }

  val tblCrawlSeeds = TableQuery[CrawlSeeds]
  val tblWrapperRules = TableQuery[WrapperRules]

  val a = (for{
    isRes <- tblCrawlSeeds.map(c => (c.title, c.urlPattern, c.status)) += ("AA", "BB", "CC")
    isRes.
  } yield ()).transactionally

  val insertNewSeed = DBIO.seq(

  )


}
