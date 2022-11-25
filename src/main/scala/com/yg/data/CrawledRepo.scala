package com.yg.data

import slick.jdbc.MySQLProfile.api._
import java.sql.Timestamp

object CrawledRepo {
  case class CrawlUnit(
                        crawlNo: Long = -1L,
                        url: Option[String] = None,
                        anchorText: Option[String] = None,
                        anchorImg: Option[String] = None,
                        status: Option[String] = None,
                        seedNo: Long = -1L,
                        pageDate: Option[String]= None,
                        regDate: Option[Timestamp] = None,
                        updDate: Option[Timestamp] = None,
                        pageText: Option[String] = None,
                        pageTitle: Option[String] = None,
                        parsedPageDate: Option[Timestamp] = None
                      )

  class CrawlUnitTableSchema(tag: Tag) extends Table[CrawlUnit](tag, None,"CRAWL_UNIT1") {
    def crawlNo = column[Long]("CRAWL_NO", O.PrimaryKey, O.AutoInc)
    def url = column[Option[String]]("URL")
    def anchorText = column[Option[String]]("ANCHOR_TEXT")
    def anchorImg = column[Option[String]]("ANCHOR_IMG")
    def status = column[Option[String]]("STATUS")
    def seedNo = column[Long]("SEED_NO")
    def pageDate = column[Option[String]]("PAGE_DATE")
    def regDate = column[Option[Timestamp]]("REG_DATE")
    def updDate = column[Option[Timestamp]]("UPD_DATE")
    def pageText = column[Option[String]]("PAGE_TEXT")
    def pageTitle = column[Option[String]]("PAGE_TITLE")
    def parsedPageDate = column[Option[Timestamp]]("PARSED_PAGE_DATE")

    def * = (crawlNo, url, anchorText, anchorImg, status, seedNo, pageDate, regDate,
      updDate, pageText, pageTitle, parsedPageDate) <> (CrawlUnit.tupled, CrawlUnit.unapply)
  }

  val crawlUnitsQuery = TableQuery[CrawlUnitTableSchema]
  val createSchemaAction = (crawlUnitsQuery.schema).create

  def findAll(seedNo: Long) = crawlUnitsQuery.filter(_.seedNo === seedNo)
    .sortBy(_.crawlNo.desc)
    .drop(0)
    .take(20)

  def getCrawledData(crawlNo: Long) = crawlUnitsQuery.filter(_.crawlNo === crawlNo)

//  val findAll2 = {
//    for{
//      cu <- crawlUnitsQuery.filter(_.seedNo === 9L)
//    } yield (cu.crawlNo, cu.url, cu.anchorText, cu.regDate)
//  }


}
