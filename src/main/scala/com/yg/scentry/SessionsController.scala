package com.yg.scentry

import org.scalatra._
import com.yg.scentry.auth.AuthenticationSupport

class SessionsController extends ScalatraServlet with AuthenticationSupport {

  before("/new") {
    logger.info("SessionsController: checking whether to run RememberMeStrategy: " + !isAuthenticated)

    if(!isAuthenticated) {
      scentry.authenticate("RememberMe")
    }
  }

  get("/new") {
    if (isAuthenticated) redirect("/")

    contentType="text/html"
    <html>

    </html>
  }

  post("/") {
    scentry.authenticate()

    if (isAuthenticated) {
      redirect("/")
    } else {
      redirect("/sessions/new")
    }
  }

  // Never do this in a real app. State changes should never happen as a result of a GET request. However, this does
  // make it easier to illustrate the logout code.
  get("/logout") {
    scentry.logout()
    redirect("/")
  }

}