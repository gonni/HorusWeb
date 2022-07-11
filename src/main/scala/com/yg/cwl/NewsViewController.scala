package com.yg.cwl

import com.yg.api.SlickRoutes
import com.yg.data.DbHorusCrawled
import org.scalatra.forms.FormSupport
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.slf4j.LoggerFactory
import org.scalatra.i18n.I18nSupport
import slick.jdbc.MySQLProfile.api._

trait NewsViewProcessing extends ScalatraServlet
  with FormSupport with I18nSupport with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/hot") {
    val seedNo = params("seedNo").toInt
    logger.info("Request Hot-News {}", seedNo)
    db.run(DbHorusCrawled.findAll2.result) map {xs =>
      xs map {
        row => {
          println("row " + row)
        }
      }
    }

    val newsPage =com.yg.report.html.news.render()
    layouts.html.dashboard.render("News", newsPage)
  }
}

class NewsViewController(val db: Database) extends ScalatraServlet with FutureSupport with NewsViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}
