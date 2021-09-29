package com.yg.api

import org.scalatra._

class HttpSampleController  extends ScalatraServlet {

  get("/hello") {
    com.yg.api.html.hello.render(new java.util.Date)
  }
}
