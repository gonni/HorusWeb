package com.yg.auth
import org.scalatra._
import org.slf4j.{Logger, LoggerFactory}

class AuthDemoController extends ScalatraServlet with AuthenticationSupport {
  val logger =  LoggerFactory.getLogger(getClass)

  before() {
    logger.info("detected sec before ..")
    contentType = "text/html"
    basicAuth
  }

  get("/*") {
    logger.info("detected sec request ..")
    val user: User = basicAuth().get

    <html>
      <body>
        <h1>Hello '{user.id}' from Scalatra Basic Authentication Demo</h1>
        <p>You are authenticated.</p>
      </body>
    </html>
  }

}