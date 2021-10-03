package com.yg.api

import org.slf4j.{Logger, LoggerFactory}
import org.scalatra._
import org.scalatra.i18n.I18nSupport
import org.scalatra.forms._

case class ValidationForm(text: String, email: Option[String], number: Option[Int], checkbox: Seq[String])

class FormsController extends ScalatraServlet with FormSupport with I18nSupport {
  val logger =  LoggerFactory.getLogger(getClass)

  val form = mapping(
    "text"     -> label("Text", text(required, maxlength(100))),
    "email"    -> label("Email", optional(text(pattern(".+@.+"), maxlength(20)))),
    "number"   -> label("Number", optional(number())),
    "checkbox" -> list(text())
  )(ValidationForm.apply)

  get("/") {
    logger.info("detected form")
    html.form()
  }

  post("/") {
    logger.info("detected form post")

    validate(form)(
      errors => BadRequest(html.form()), form => html.result(form)
    )
  }

}
