package com.yg.api

import org.scalatra._
import org.slf4j.{Logger, LoggerFactory}

class FormSampleController extends ScalatraServlet {
  val logger = LoggerFactory.getLogger(getClass)

}

case class ValidationForm(
                           text: String,
                           email: Option[String],
                           number: Option[Int],
                           checkbox: Seq[String]
                         )