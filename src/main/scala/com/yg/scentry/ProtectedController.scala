package com.yg.scentry

import org.scalatra._
import com.yg.scentry.auth.AuthenticationSupport

class ProtectedController extends ScalatraServlet with AuthenticationSupport {

  /**
   * Require that users be logged in before they can hit any of the routes in this controller.
   */
  before() {
    requireLogin()
  }

  get("/") {
    "This is a protected controller action. If you can see it, you're logged in."
  }
}
