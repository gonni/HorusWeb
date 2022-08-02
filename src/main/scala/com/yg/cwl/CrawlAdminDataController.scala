package com.yg.cwl

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

class CrawlAdminDataController extends ScalatraServlet with FormSupport with I18nSupport {
  val logger = LoggerFactory.getLogger(getClass)

  case class ListSeedFormSchema(
                                 urlPattern: String,
                                 listUrlPattern: String,
                                 listTopAreaFilter: String
                               )

  val form = mapping(
    "urlPattern" -> label("urlPattern", text(required, maxlength(20))),
    "listUrlPattern" -> label("listUrlPattern", text()),
    "listTopAreaFilter" -> label("listTopAreaFilter", text())
  )(ListSeedFormSchema.apply)

  post("/seed/list/validate") {
    logger.info(s"detected validate the seed wrapping options with ${form}")

    validate(form)(
      error => {
        logger.info("detected FAIL")
        html.message("Seed Creation Failed", "Detected Invalid Parameters !!", s"Form -> ${form}")
      },
      formOri => {
        logger.info(s"detected valid data to validate wrapping options -> ${formOri}")
      })
  }
}
