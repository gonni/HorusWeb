package com.yg.api

import org.scalatra._

class MyScalatraServlet extends ScalatraServlet {

  get("/hell") {
    views.html.hello()
  }

}
