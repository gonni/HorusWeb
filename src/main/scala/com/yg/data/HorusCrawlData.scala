package com.yg.data

import org.scalatra.{FutureSupport, ScalatraBase, ScalatraServlet}
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

object CrawlTables {
  class CrawlSeeds(tag: Tag) extends Table[(Int, String, String, String)](tag, "CRAWL_SEEDS9") {
    def seedNo = column[Int]("SEED_NO", O.PrimaryKey) // This is the primary key column
    def urlPattern = column[String]("URL_PATTERN")
    def title = column[String]("TITLE")
    def status = column[String]("STATUS")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (seedNo, urlPattern, title, status)
  }

  val crawlSeeds = TableQuery[CrawlSeeds]

  val createSchemaAction = (crawlSeeds.schema).create

  val insertCrawlSeed = DBIO.seq(
    CrawlTables.crawlSeeds ++= Seq(
      (98, "http://test.yg.com", "Test", "TEST")
    )
  )

  val findAllFromCrawlSeeds = {
    for {
      ci <- crawlSeeds
    } yield (ci.seedNo, ci.title, ci.urlPattern, ci.status)
  }
}

trait CrawlRoute extends ScalatraBase with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/crawl/db/createTable") {
    logger.info("create table crawl_unit9")
    db.run(CrawlTables.createSchemaAction)
  }

  get("/crawl/db/crawlUnits") {
    logger.info("get crawl_unit9")
    contentType = "text/plain"

    db.run(CrawlTables.findAllFromCrawlSeeds.result) map {
      xs => xs map {case(s1, s2, s3, s4) => f"$s1 / $s2 / $s3 / $s4" } mkString("\n")
    }
  }
}

class HorusCrawlData(val db: Database) extends ScalatraServlet with FutureSupport with CrawlRoute {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
