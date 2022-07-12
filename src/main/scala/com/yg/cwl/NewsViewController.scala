package com.yg.cwl

import com.yg.api.SlickRoutes
import com.yg.data.DbHorusCrawled
import com.yg.data.DbHorusCrawled.CrawlUnit
import org.scalatra.forms.FormSupport
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.slf4j.LoggerFactory
import org.scalatra.i18n.I18nSupport
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

trait NewsViewProcessing extends ScalatraServlet
  with FormSupport with I18nSupport with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/hot") {
    val seedNo = params("seedNo").toInt
    logger.info(" -> Request Hot-News {}", seedNo)
//    db.run(DbHorusCrawled.findAll(9).map(row => (row.url, row.anchorText)).result) map {
//      rows => {
//        rows map {
//          row => println(row)
//        }
//      }
//    }

//    db.run(DbHorusCrawled.findAll2.result).map {rows => {
//      rows.map(println)
//    }}

//    println("-----")

//    db.run(DbHorusCrawled.findAll3(9L).result).map(
//      println
//    )

    val res : Future[Seq[CrawlUnit]] = db.run[Seq[CrawlUnit]](DbHorusCrawled.findAll(9).result)
    res.foreach(a => {
      a map {
        ab => {
          println("===>" + ab)
        }
      }
    })


    val newsPage =com.yg.report.html.news.render()
    layouts.html.dashboard.render("News", newsPage)
  }
}

class NewsViewController(val db: Database) extends ScalatraServlet with FutureSupport with NewsViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
