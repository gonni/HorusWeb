package com.yg.cwl

import com.yg.data.{CrawlSeed, DbHorus}
import org.scalatra._
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import scala.collection.mutable.ArrayBuffer

trait CrawlAdminViewProcessing extends ScalatraServlet with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/seeds") {
    logger.info("Request seeds by Admin ..")

    var lss = ArrayBuffer[CrawlSeed]()
    db.run(DbHorus.findAllFromCrawlSeeds.result) map {
      xs => xs map {
        case(a, b, c, d) => {
          lss += CrawlSeed(a, b, c, d)
        }
      }
    }

    com.yg.cwl.html.seeds(lss)
  }

  post("/seed/new") {
//    com.yg.cwl.html.seeds()
    "AAA"
  }
}

class CrawlAdminViewController(val db: Database)
  extends ScalatraServlet with FutureSupport with CrawlAdminViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}