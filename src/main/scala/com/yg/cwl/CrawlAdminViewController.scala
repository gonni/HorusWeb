package com.yg.cwl

import org.scalatra._

class CrawlAdminViewController extends ScalatraServlet {
  get("/seeds") {
    com.yg.cwl.html.seeds()
  }
}
