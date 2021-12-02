package com.yg.api

import com.yg.data._
import org.scalatra._
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._
//import scala.concurrent.ExecutionContext.Implicits.global

trait HorusRoute extends ScalatraBase with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/seeds") {
    logger.info("get crawl_unit9")
    contentType = "text/plain"

    db.run(DbHorus.findAllFromCrawlSeeds.result) map {
      xs => xs.collect()
    }
  }
}

class HorusViewController(val db: Database) extends ScalatraServlet with FutureSupport with HorusRoute {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

  get("/d/seeds") {
    val data = List(
      CrawlSeed(1L, "Https://www.naver.com", "Naver",  "Good"),
      CrawlSeed(2L, "Https://www.dakao.com", "Daum",  "Good"),
      CrawlSeed(3L, "Https://www.nate.com", "Nate", "Bad"),
      )
//      com.yg.api.html
      com.yg.api.html.horus.render(data)
  }
}
