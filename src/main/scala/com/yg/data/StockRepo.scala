package com.yg.data

import slick.jdbc.MySQLProfile.api._
import java.sql.Timestamp

object StockRepo {
  case class EndPriceAnalyzeResult(
    targetDt: String,
    itemCode: String,
    matchScore: Double,
    basePrice: Int,
    nextDayHigh5m: Int,
    afterDayHigh5d: Int,
    updDt: Timestamp,
    memo: String
  )

  class EnpriceAnalyzeResultSchema(tag: Tag) extends Table[EndPriceAnalyzeResult](tag, None, "STOCK_END_PRICE_ANALYZE_RESULT") {
    def targetDt = column[String]("TARGET_DT")
    def itemCode = column[String]("ITEM_CODE")
    def matchScore = column[Double]("MATCH_SCORE")
    def basePrice = column[Int]("BASE_PRICE")
    def nextDayHigh5m = column[Int]("NEXT_DAY_HIGH5M")
    def afterDayHigh5d = column[Int]("AFTER_DAY_HIGH5D")
    def updDt = column[Timestamp]("UPD_DT")
    def memo = column[String]("MEMO")

    def * = (targetDt, itemCode, matchScore, basePrice, nextDayHigh5m, afterDayHigh5d, updDt, memo) <> 
      (EndPriceAnalyzeResult.tupled, EndPriceAnalyzeResult.unapply)
  }

  val endPriceAnalyzeResultQuery = TableQuery[EnpriceAnalyzeResultSchema]

  def selectAll() = endPriceAnalyzeResultQuery
}
