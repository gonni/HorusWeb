package com.yg
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import org.scalatra.servlet.ScalatraListener

object JettyLaunchMain {
  def main(args: Array[String]): Unit = {
    println("Active Horus-web ..")

    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 18090

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    //    context.setInitParameter(ScalatraListener.LifeCycleKey, "ScalatraBootstrap")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start
    server.join
  }
}
