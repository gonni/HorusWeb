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

//    var ls = new util.ArrayList[CrawlSeed]()
    var lss = ArrayBuffer[CrawlSeed]()
    db.run(DbHorus.findAllFromCrawlSeeds.result) map {
          xs => xs map {
            case(a, b, c, d) => {
//              println("aaa")
//              ls.add(CrawlSeed(a, b, c, d))
              lss += CrawlSeed(a, b, c, d)
            }
          }
          com.yg.api.html.horus.render(lss)
    }
  }

  get("/inputSeed") {
    val title = params("title")
    val urlPattern = params("urlp")
    val status = params("status")


    val paramMap = request.getParameterMap()


    logger.info("Need to Data Add to DB ..")
//    redirect("/horus/seeds")
    s"Data($title | $urlPattern | $status) inserted at " + System.currentTimeMillis
  }

  get("/newSeed") {
    logger.info("Detected Input Seed Page ..")
    com.yg.api.html.horusForm()
  }
}

class HorusViewController(val db: Database) extends ScalatraServlet with FutureSupport with HorusRoute {
//  val logger2 = LoggerFactory.getLogger(getClass)
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global



  get("/d/seeds") {
    "AA"
  }
}
