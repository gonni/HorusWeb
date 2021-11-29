import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import com.yg._
import org.scalatra._

import javax.servlet.ServletContext
import com.mysql.jdbc._
import com.yg.api._
import com.yg.auth.AuthDemoController
import com.yg.data.HorusCrawlData
import com.yg.scentry._

//import slick.jdbc.H2Profile.api._
import slick.jdbc.MySQLProfile.api._

class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  override def init(context: ServletContext) {
    val db = Database.forDataSource(cpds, None)
//    context.mount(new SlickApp(db), "/*")
//    context.mount(new RestSampleController(), "/rest/*")

    context.mount(new AuthDemoController(), "/*")
//    context.mount(new FormsController(), "/usr/*")

//    context.mount(new ProtectedController(), "/*")
    context.mount(new HttpSampleController(), "/hell/*")
    context.mount(new HorusCrawlData(db), "/crawl/*")
//    context.mount(new SessionsController(), "/sessions/*")

//    context.mount(new HttpSampleController(), "/html/*")

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
