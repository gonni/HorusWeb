package com.yg

import org.scalatra._

class MyScalatraServlet extends ScalatraServlet {

  get("/hell") {
    views.html.hello()
  }

}
