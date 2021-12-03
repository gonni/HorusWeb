package com.yg.data

import slick.jdbc.MySQLProfile.api._
object DbHorus {
  class CrawlSeeds(tag: Tag) extends Table[(Int, String, String, String)](tag, "CRAWL_SEEDS8") {
    def seedNo = column[Int]("SEED_NO", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def urlPattern = column[String]("URL_PATTERN")
    def title = column[String]("TITLE")
    def status = column[String]("STATUS")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (seedNo, urlPattern, title, status)
  }

  val crawlSeeds = TableQuery[CrawlSeeds]

  val createSchemaAction = (crawlSeeds.schema).create

  val insertCrawlSeed = DBIO.seq(
    CrawlTables.crawlSeeds.map(c => (c.urlPattern, c.title, c.status))
      += ("http://test.yg.com", "Test_" + System.currentTimeMillis(), "TEST")
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
    } yield (ci.seedNo, ci.title, ci.urlPattern, ci.status)
  }
}
