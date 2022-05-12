package com.yg.cwl

import org.scalatra._
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

trait ReportViewProcessing extends ScalatraServlet
  with FormSupport with I18nSupport with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/rttf") {
    logger.info("Request RT-TF report page ..")

    val rttf = com.yg.report.html.rttf()
    layouts.html.dashboard.render("RealTime Term-F Main", rttf)
  }
}

class ReportViewController(val db: Database)
  extends ScalatraServlet with FutureSupport with ReportViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}
