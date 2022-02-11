package com.yg.cwl

import com.yg.data.{CrawlSeed, DbHorus, HorusSlick}
import org.scalatra._
import org.slf4j.LoggerFactory
import play.twirl.api.Html
import slick.jdbc.MySQLProfile.api._
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

import scala.:+
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ReportViewProcessing extends ScalatraServlet
  with FormSupport with I18nSupport with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/rttf") {
    logger.info("Request RT-TF report page ..")

    com.yg.report.html.rttf()
  }
}

class ReportViewController(val db: Database)
  extends ScalatraServlet with FutureSupport with ReportViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}
