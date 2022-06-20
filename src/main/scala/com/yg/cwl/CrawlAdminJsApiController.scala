package com.yg.cwl

import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

class CrawlAdminJsApiController extends ScalatraServlet with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  before() {
    contentType = formats("json")
  }



}
