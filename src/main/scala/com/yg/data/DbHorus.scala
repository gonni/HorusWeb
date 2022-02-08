package com.yg.data

import slick.jdbc.MySQLProfile.api._
object DbHorus {
  class CrawlSeeds(tag: Tag) extends Table[(Int, String, String, String)](tag, "CRAWL_SEEDS") {
    def seedNo = column[Int]("SEED_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def urlPattern = column[String]("URL_PATTERN")
    def title = column[String]("TITLE")
    def status = column[String]("STATUS")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (seedNo, urlPattern, title, status)

  }

  case class CrawlSeed(
                        seedNo: Option[Int],
                        urlPattern: String,
                        title: String,
                        status: String
                      )

  class CrawlSeeds2(tag: Tag) extends Table[CrawlSeed](tag, "CRAWL_SEEDS") {
    def seedNo = column[Int]("SEED_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def urlPattern = column[String]("URL_PATTERN")
    def title = column[String]("TITLE")
    def status = column[String]("STATUS")

    def * =
      (seedNo.?, urlPattern, title, status) <> (CrawlSeed.tupled, CrawlSeed.unapply)
  }

  val crawlSeeds = TableQuery[CrawlSeeds2]

  val createSchemaAction = (crawlSeeds.schema).create

  val insertCrawlSeed = DBIO.seq(
    CrawlTables.crawlSeeds.map(c => (c.urlPattern, c.title, c.status))
      += ("http://test.yg1.com", "Test1_" + System.currentTimeMillis(), "TEST1")
  )

  val insertNewSeed = (title: String, url: String, status: String) => {
    DBIO.seq(
      CrawlTables.crawlSeeds.map(c => (c.urlPattern, c.title, c.status))
        += (url, title, status)
    )
  }

  val findAllFromCrawlSeeds = {
    for {
      ci <- crawlSeeds
    } yield (ci.seedNo, ci.urlPattern, ci.title, ci.status)
  }
}
