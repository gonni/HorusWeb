import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import com.yg._
import org.scalatra._

import javax.servlet.ServletContext
import com.mysql.jdbc._
import com.yg.api._
import com.yg.auth.AuthDemoController
import com.yg.cwl.{CrawlAdminDataController, CrawlAdminJsApiController, CrawlAdminViewController, CrawlConfController, ReportViewController}
import com.yg.data.deprecated.HorusCrawlData
import com.yg.news.{NewsDataApiController, NewsJsController, NewsViewController}
import com.yg.scentry._

//import slick.jdbc.H2Profile.api._
import slick.jdbc.MySQLProfile.api._

class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  override def init(context: ServletContext) {
    val rtConf = RuntimeConfig()
    println("-------------------------------------------")
    println(s"Horus Web - ${RuntimeConfig().getString("profile.name")}")
    println("-------------------------------------------")
    println(RuntimeConfig())
    println("-------------------------------------------")

//    val db = Database.forDataSource(cpds, None)
    val db = Database.forURL(url = RuntimeConfig("mysql.url"),
      user = RuntimeConfig("mysql.user"),
      password = RuntimeConfig("mysql.password"),
      driver = "com.mysql.jdbc.Driver")

    context.mount(new NewsDataApiController(db), "/news/data/*")
    context.mount(new HttpSampleController(), "/hell/*")
    context.mount(new HorusCrawlData(db), "/crawl/*")
    context.mount(new NewsViewController(db), "/news/*")
    context.mount(new NewsJsController(db), "/news/api/*")
    context.mount(new CrawlAdminJsApiController, "/core/*")
    context.mount(new CrawlConfController(db), "/conf/")
    context.mount(new CrawlAdminViewController(db), "/admin/crawl/*")
    context.mount(new CrawlAdminDataController, "/admin/config/*")
    context.mount(new ReportViewController(db), "/v/report")
    context.mount(new HorusViewController(db), "/horus/*")  // db connection

  }

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    cpds.close
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }
}
