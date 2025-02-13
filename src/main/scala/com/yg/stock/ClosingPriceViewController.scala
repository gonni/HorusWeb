package com.yg.stock

import org.scalatra._
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport
import org.slf4j.LoggerFactory
import play.twirl.api.Html
import slick.jdbc.MySQLProfile.api._

import scala.:+
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.yg.data.StockRepo
import java.sql.Timestamp

trait ClosingPriceViewControl extends ScalatraServlet with FormSupport with I18nSupport with FutureSupport {
  val logger = LoggerFactory.getLogger(getClass)
  def stockDb: Database
  
  // get("/closingPriceAnalysis") {
  //   logger.info("Request closing price analysis ..")
    
  //   stockDb.run(StockRepo.selectAll.result) map {xs => 
  //     layouts.html.dashboard.render("Seeds", com.yg.stock.html.closingPriceAnalysis.render(xs))
  //   }
  // }

  get("/closingPriceAnalysis") {
    logger.info("Request closing price analysis ..")
    
    val conv = stockDb.run(StockRepo.selelctAllwithItemName.result) map {xs => 
      xs.map { xss => 
        (EndPriceAnalyzeResultWithItemName.apply _).tupled(xss) 
      }
    }
    conv.map { xs1 =>
      layouts.html.dashboard.render("Seeds", com.yg.stock.html.closingPriceAnalysis.render(xs1))
    }
  }
}


case class EndPriceAnalyzeResultWithItemName (
  targetDt: String,
  itemCode: String,
  itemName: String,
  matchScore: Double,
  basePrice: Int,
  nextDayHigh5m: Int,
  afterDayHigh5d: Int,
  updDt: Timestamp,
  memo: String
)

class ClosingPriceViewController(val stockDb: Database) extends ScalatraServlet with FutureSupport with ClosingPriceViewControl {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global
}