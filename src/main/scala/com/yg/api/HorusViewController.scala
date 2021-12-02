package com.yg.api

import com.yg.data._
import org.scalatra._
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import java.util
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
//import scala.concurrent.ExecutionContext.Implicits.global

trait HorusRoute extends ScalatraBase with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/seeds") {
    logger.info("get crawl_unit9")
//    contentType = "text/plain"

    var ls = new util.ArrayList[CrawlSeed]()
    var lss = ArrayBuffer[CrawlSeed]()
    db.run(DbHorus.findAllFromCrawlSeeds.result) map {
          xs => xs map {
            case(a, b, c, d) => {
              println("aaa")
              ls.add(CrawlSeed(a, b, c, d))
              lss += CrawlSeed(a, b, c, d)
            }
          }

          com.yg.api.html.horus.render(lss)
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
//      com.yg.api.htmml.horus.render(data)
    "AB"
  }
}
