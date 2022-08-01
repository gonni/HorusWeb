package com.yg.data

import slick.jdbc.MySQLProfile.api._
import java.sql.Timestamp


object DbHorus {
//  class CrawlSeeds(tag: Tag) extends Table[(Int, String, String, String)](tag, "CRAWL_SEEDS") {
//    def seedNo = column[Int]("SEED_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
//    def urlPattern = column[String]("URL_PATTERN")
//    def title = column[String]("TITLE")
//    def status = column[String]("STATUS")
//
//    def * = (seedNo, urlPattern, title, status)
//  }

  class CrawlSeeds2(tag: Tag) extends Table[CrawlSeed](tag, "CRAWL_SEEDS") {
    def seedNo = column[Int]("SEED_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def urlPattern = column[String]("URL_PATTERN")
    def title = column[String]("TITLE")
    def status = column[String]("STATUS")

    def * =
      (seedNo.?, urlPattern, title, status) <> (CrawlSeed.tupled, CrawlSeed.unapply)
  }
  case class CrawlSeed(seedNo: Option[Int], urlPattern: String, title: String, status: String)
  // ---

  class WrapperRules(tag: Tag) extends Table[WrapperRule](tag, "WRAPPER_RULE") {
    def wrapperNo = column[Int]("WRAPPER_NO", O.PrimaryKey, O.AutoInc)
    def seedNo = column[Int]("SEED_NO")
    def wrapType = column[String]("WRAP_TYPE")
    def wrapVal = column[String]("WRAP_VAL")
    def wrapName = column[String]("WRAP_NAME")
    def regDt = column[Timestamp]("REG_DT")

    def * = (wrapperNo.?, seedNo, wrapType, wrapVal, wrapName, regDt) <> (WrapperRule.tupled, WrapperRule.unapply)
  }
  case class WrapperRule(wrapperNo: Option[Int], seedNo: Int, wrapType: String, wrapVal: String, wrapName: String, regDt: Timestamp)

  // --------
  val crawlSeeds = TableQuery[CrawlSeeds2]
  val wrapperRules = TableQuery[WrapperRules]

  val createSchemaAction = (crawlSeeds.schema).create

  val insertNewSeedAndGetId = (title: String, url: String, status: String) => {
    (crawlSeeds returning crawlSeeds.map(_.seedNo)) += CrawlSeed(None, url, title, status)
  }

  val insertNewSeed = (title: String, url: String, status: String) => {
    DBIO.seq(
      CrawlTables.crawlSeeds.map(c => (c.urlPattern, c.title, c.status))
        += (url, title, status)
    )
  }

  val insertWrapperRule = (wr : Seq[WrapperRule]) => {
    wrapperRules ++= wr
  }

  val findAllFromCrawlSeeds = {
    for {
      ci <- crawlSeeds
    } yield (ci.seedNo, ci.urlPattern, ci.title, ci.status)
  }

}
