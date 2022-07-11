package com.yg.data

import slick.jdbc.MySQLProfile.api._
import java.time.LocalDateTime

object DbHorusCrawled {
  case class CrawlUnit(
                      crawlNo: Option[Int],
                      url: String,
                      anchorText: String,
                      anchorImg: String,
                      status: String,
                      seedNo: Int,
                      pageDate: String,
                      regDate: LocalDateTime,
                      updDate: LocalDateTime,
                      pageText: String,
                      pageTitle: String,
                      parsedPageDate: LocalDateTime
                      )

  class CrawlUnitTableSchema(tag: Tag) extends Table[CrawlUnit](tag, "CRAWL_UNIT1") {
    def crawlNo = column[Int]("CRAWL_NO", O.PrimaryKey, O.AutoInc)
    def url = column[String]("URL")
    def anchorText = column[String]("ANCHOR_TEXT")
    def anchorImg = column[String]("ANCHOR_IMG")
    def status = column[String]("STATUS")
    def seedNo = column[Int]("SEED_NO")
    def pageDate = column[String]("PAGE_DATE")
    def regDate = column[LocalDateTime]("REG_DATE")
    def updDate = column[LocalDateTime]("UPD_DATE")
    def pageText = column[String]("PAGE_TEXT")
    def pageTitle = column[String]("PAGE_TITLE")
    def parsedPageDate = column[LocalDateTime]("PARSED_PAGE_DATE")

    def * = (crawlNo.?, url, anchorText, anchorImg, status, seedNo, pageDate, regDate,
      updDate, pageText, pageTitle, parsedPageDate) <> (CrawlUnit.tupled, CrawlUnit.unapply)
  }

  val crawlUnitsQuery = TableQuery[CrawlUnitTableSchema]
  val createSchemaAction = (crawlUnitsQuery.schema).create

  def findAll(seedNo1: Int) = crawlUnitsQuery.filter(_.seedNo === seedNo1)
    .drop(0)
    .take(25).result

  val findAll2 = {
    for{
      cu <- crawlUnitsQuery
    } yield (cu.crawlNo, cu.url, cu.anchorText)
  }


}
