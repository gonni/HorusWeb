package com.yg.cwl

import com.yg.conn.CrawlCoreClient
import com.yg.data.{CrawlSeed, CrawlSeedWithJob, DbHorus, HorusSlick}
import org.scalatra._
import org.slf4j.LoggerFactory
import play.twirl.api.Html
import slick.jdbc.MySQLProfile.api._
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

import scala.:+
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

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



  get("/exseeds") {
    logger.info("Request eXseeds by Admin ..")

    val jobInfo = CrawlCoreClient.crawlJobStatus(21)
    def getJobStatus(seedNo : Int) = {
      jobInfo.filter(_.seedNo == seedNo)
    }

    val lss = ArrayBuffer[CrawlSeedWithJob]()
    db.run(DbHorus.findAllFromCrawlSeeds.result) map {
      xs => xs map {
        case(a, b, c, d) => {
          val jobs = getJobStatus(a)
          lss += CrawlSeedWithJob(a, b, c, d, jobs)
        }
      }
        layouts.html.dashboard.render("Seeds", com.yg.cwl.html.exseeds.render(lss))
    }

  }



  get("/seed/new") {
    logger.info("Request create & manage new seed")
    val main = com.yg.cwl.html.seedInput()
    layouts.html.dashboard.render("Dashboard Template Main", main)

  }

  get("/seed/pagelab") {
    logger.info("Request Research Page Wrapping ..")
    val main = com.yg.cwl.html.researchPage()
    layouts.html.dashboard.render("Research Page DOM", main)
  }

  case class ValidateNewSeedForm (
    crawlTitle: String,
    urlPattern: String,
    listUrlPattern: String,
    listTopAreaFilter: String,
    contTitleInPage: String,
    contMainCont: String,
    contDateOnPage: String
  )

  val newSeedForm = mapping(
    "crawlTitle" -> label("title", text(required, maxlength(10))),
    "urlPattern" -> label("urlPattern", text(required, maxlength(20))),
    "listUrlPattern" -> label("listUrlPattern", text()),
    "listTopAreaFilter" -> label("listTopAreaFilter", text()),
    "contTitleInPage" -> label("contTitleInPage", text()),
    "contMainCont" -> label("contMainCont", text()),
    "contDateOnPage" -> label("contDateOnPage", text()),
  )(ValidateNewSeedForm.apply)

  get("/seed/validate") {
    logger.info("Add new seed action detected ..")
//
    validate(newSeedForm)(
      errors => {
        logger.info("detected FAIL")
        BadRequest(html.error)
      },
      newSeedForm => {
        logger.info("detected success")
        html.message("Success", "New Seed Validated!!", s"Form -> ${newSeedForm}")
      }
    )
  }

  get("/seed/register") {
    logger.info(s"Register new action detected with ${newSeedForm}")

    validate(newSeedForm)(
      errors => {
        logger.info("detected FAIL")
//        BadRequest(html.error)
        html.message("Seed Creation Failed", "Detected Invalid Parameters !!", s"Form -> ${newSeedForm}")
      },
      newSeedForm => {
        logger.info(s"detected success with ${newSeedForm}")

        var params = List[(String, String)]()
        if(newSeedForm.listUrlPattern != null && newSeedForm.listUrlPattern.trim.length > 0)
          params = params :+ ("TEST_LIST_URL_PATTERN", newSeedForm.listUrlPattern)
        if(newSeedForm.listTopAreaFilter != null && newSeedForm.listTopAreaFilter.trim.length > 0)
          params = params :+ ("TEST_LIST_URL_PATTERN", newSeedForm.listTopAreaFilter)

        logger.info(s"Params -> ${params}")

        val seedId = Await.result(
          db.run(HorusSlick.registerAll(
            HorusSlick.CrawlSeed(None, newSeedForm.urlPattern, newSeedForm.crawlTitle, "TEST"),
            params).transactionally
          ),
          Duration.Inf
        )

//        html.message("Success", "New Seed Validated!!", s"Form -> ${newSeedForm}")
        redirect("/admin/crawl/seeds")
      }
    )
//    html.message("Success", "New Seed Registerd", "Registered to DB as a id " + seedId.get)
  }


  get("/error") {
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