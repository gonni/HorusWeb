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

  case class CrawlSeed(seedNo: Option[Int], urlPattern: String, title: String, status: String)

  class CrawlSeeds(tag: Tag)
    extends Table[(Int, String, String, String)](tag, "CRAWL_SEEDS") {

    def seedNo = column[Int]("SEED_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def urlPattern = column[String]("URL_PATTERN")
    def title = column[String]("TITLE")
    def status = column[String]("STATUS")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (seedNo, urlPattern, title, status)
  }

  class CrawlSeedsV2(tag: Tag)
    extends Table[CrawlSeed](tag, "CRAWL_SEEDS") {

    def seedNo = column[Option[Int]]("SEED_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def urlPattern = column[String]("URL_PATTERN")
    def title = column[String]("TITLE")
    def status = column[String]("STATUS")

    // Every table needs a * projection with the same type as the table's type parameter
    def * =
      (seedNo, urlPattern, title, status) <> (CrawlSeed.tupled, CrawlSeed.unapply)
  }

  class WrapperRulesV2(tag: Tag)
    extends Table[WrapperRule](tag, "WRAPPER_RULE") {

    def wrapperNo = column[Option[Int]]("WRAPPER_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def seedNo = column[Int]("SEED_NO")
    def wrapType = column[String]("WRAP_TYPE")
    def wrapVal = column[String]("WRAP_VAL")
    def wrapName = column[String]("WRAP_NAME")
    def regDt = column[Date]("REG_DT")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (wrapperNo, seedNo, wrapType, wrapVal, wrapName, regDt) <> (WrapperRule.tupled, WrapperRule.unapply)
  }

  case class WrapperRule(wrapperNo: Option[Int],
                         seedNo: Int,
                         wrapType: String,
                         wrapVal: String,
                         wrapName: String,
                         regDt: Date)

  class WrapperRules(tag: Tag)
    extends Table[(Option[Int], Int, String, String, String, Date)](tag, "WRAPPER_RULE") {

    def wrapperNo = column[Option[Int]]("WRAPPER_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def seedNo = column[Int]("SEED_NO")
    def wrapType = column[String]("WRAP_TYPE")
    def wrapVal = column[String]("WRAP_VAL")
    def wrapName = column[String]("WRAP_NAME")
    def regDt = column[Date]("REG_DT")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (wrapperNo, seedNo, wrapType, wrapVal, wrapName, regDt)
  }

  val tblCrawlSeeds = TableQuery[CrawlSeedsV2]
  val tblWrapperRules = TableQuery[WrapperRulesV2]

  val a = (for{
    isRes <- tblCrawlSeeds.map(c => (c.title, c.urlPattern, c.status)) += ("AA", "BB", "CC")

  } yield ()).transactionally

//  val insertNewSeed = DBIO.seq(
//
//  )


//  val newSeed = (tblCrawlSeeds returning tblCrawlSeeds.map(_.seedNo)) += CrawlSeed(None, urlPattern, title, "TS")

//  def insertNewSeed = (crawlSeed: CrawlSeed) => {
//    (tblCrawlSeeds returning tblCrawlSeeds.map(_.seedNo)) += crawlSeed
//  }
  def insertNewSeed(crawlSeed: CrawlSeed) =
    (tblCrawlSeeds returning tblCrawlSeeds.map(_.seedNo)) += crawlSeed

  def insertWrapperRule(wrapperRule: WrapperRule) =
    tblWrapperRules += wrapperRule

  def register(crawlSeed: CrawlSeed, a: String, b: String) = {
    for {
      seedId <- insertNewSeed(crawlSeed)
      _ <- insertWrapperRule(WrapperRule(None, seedId.get, a, b, "C", new Date(System.currentTimeMillis)))
    } yield seedId
  }
//  val insertAllNewSeed = (title: String, urlPattern: String, rule: Map[String, String]) => {
//    (for(
//      seedNo <- ((tblCrawlSeeds returning tblCrawlSeeds.map(_.seedNo)) += CrawlSeed(None, urlPattern, title, "TS"))
//      _ <- (tblWrapperRules.map(m => (m.wrapperNo, m.seedNo)) += (1, 2))
//    ) yield())
//
//  }

}
