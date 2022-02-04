package com.yg.cwl

import com.yg.data.{CrawlSeed, DbHorus}
import org.scalatra._
import org.slf4j.LoggerFactory
import play.twirl.api.Html
import slick.jdbc.MySQLProfile.api._
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

import scala.collection.mutable.ArrayBuffer

trait CrawlAdminViewProcessing extends ScalatraServlet
  with FormSupport with I18nSupport with FutureSupport{
  val logger = LoggerFactory.getLogger(getClass)

  def db: Database

  get("/seeds") {
    logger.info("Request seeds by Admin ..")

    var lss = ArrayBuffer[CrawlSeed]()
    db.run(DbHorus.findAllFromCrawlSeeds.result) map {
        xs => xs map {
          case(a, b, c, d) => {
            lss += CrawlSeed(a, b, c, d)
          }
        }
        layouts.html.dashboard.render("Seeds", com.yg.cwl.html.seeds.render(lss))
    }
  }

  get("/seed/new") {
    logger.info("Request create & manage new seed")
    val main = com.yg.cwl.html.seedInput.render()
    layouts.html.dashboard.render("Dashboard Template Main", main)
  }

  case class ValidateNewSeedForm (
    crawlTitle: String,
    urlPattern: String,
    listUrlPattern: Option[String],
    listTopAreaFilter: Option[String],
    contTitleInPage: Option[String],
    contMainCont: Option[String],
    contDateOnPage: Option[String]
  )

  val newSeedForm = mapping(
    "crawlTitle" -> label("title", text(required, maxlength(10))),
    "urlPattern" -> label("lblUrlPattern", text(required, maxlength(20))),
    "listUrlPattern" -> label("", ???),
    "listTopAreaFilter" -> label("", ???),
    "contTitleInPage" -> label("", ???),
    "contMainCont" -> label("", ???),
    "contDateOnPage" -> label("", ???),
  )(ValidateNewSeedForm.apply)

  post("/seed/validate") {
    logger.info("Add new seed action detected ..")

//    validate(newSeedForm)(
//      errors : Seq[(String, String)] => {
////        BadRequest(html.error(errors))
//        ???
//      },
//      form : ValidateNewSeedForm => {
//        ???
//      }
//    )

//    validate(newSeedForm)(
//      hasErrors: Seq[(String, String)] => {
//        ???
//      },
//      successes: ValidateNewSeedForm => {
//        ???
//      }
//    )
  }

  get("/main0") {
    logger.info("Requested Main HTML Template ..")

//    val body : Html
//    = Html("<h1>This text is made by Template Body</h1>")

    val body = layouts.html.rttf.render()
    layouts.html.crawlframe.render("Template Main", body)
  }

  get("/main") {
    logger.info("Requested Main Layout HTML Template ..")
    val main = com.yg.cwl.html.main.render()
    layouts.html.dashboard.render("Dashboard Template Main", main)
  }
}

class CrawlAdminViewController(val db: Database)
  extends ScalatraServlet with FutureSupport with CrawlAdminViewProcessing {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}