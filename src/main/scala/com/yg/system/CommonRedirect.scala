package com.yg.system

import org.scalatra.ScalatraServlet
import org.slf4j.LoggerFactory

trait CommonRedirect extends ScalatraServlet{
  val logger = LoggerFactory.getLogger(getClass)

  get("/") {
    logger.info(s"root request from ${request.getRemoteAddr} detected ..")
    redirect("/news/hot?seedNo=21")
  }

  get("/about") {
    layouts.html.dashboard.render("About Horus", com.yg.me.html.about.render())
  }
}

class CommonRedirectController extends ScalatraServlet with CommonRedirect