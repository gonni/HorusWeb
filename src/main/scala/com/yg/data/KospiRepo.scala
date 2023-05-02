package com.yg.data

import slick.jdbc.MySQLProfile.api._

object KospiRepo {

  case class KospiData(
                  targetDt: String,
                  indexValue: Option[Double] = None,
                  indexUp: Option[Boolean] = Some(false),
                  diffAmount: Option[Double] = None,
                  upDownPer: Option[Double] = None,
                  totalEa: Option[Double] = None,
                  totalVolume: Option[Double] = None,
                  ant: Option[Double] = None,
                  foreigner: Option[Double] = None,
                  company: Option[Double] = None,
                  investBank: Option[Double] = None,
                  insurance: Option[Double] = None,
                  investTrust: Option[Double] = None,
                  bank: Option[Double] = None,
                  etcBank: Option[Double] = None,
                  pensionFund: Option[Double] = None
                  )

  class CrawlKospiMapping(tag: Tag) extends Table[KospiData](tag, None, "CRAWL_KOSPI") {
    def targetDt = column[String]("TARGET_DT", O.PrimaryKey)
    def indexValue = column[Option[Double]]("INDEX_VALUE")
    def indexUp = column[Option[Boolean]]("INDEX_UP")
    def diffAmount = column[Option[Double]]("DIFF_AMOUNT")
    def upDownPer = column[Option[Double]]("UP_DOWN_PER")
    def totalEa = column[Option[Double]]("TOTAL_EA")
    def totalVolume = column[Option[Double]]("TOTAL_VOLUME")
    def ant = column[Option[Double]]("ANT")
    def foreigner = column[Option[Double]]("FOREIGNER")
    def company = column[Option[Double]]("COMPANY")
    def investBank = column[Option[Double]]("INVEST_BANK")
    def insurance = column[Option[Double]]("INSURANCE")
    def investTrust = column[Option[Double]]("INVEST_TRUST")
    def bank = column[Option[Double]]("BANK")
    def etcBank = column[Option[Double]]("ETC_BANK")
    def pensionFund = column[Option[Double]]("PENSION_FUND")

    override def * = (targetDt, indexValue, indexUp, diffAmount, upDownPer, totalEa, totalVolume, ant,
      foreigner, company, investBank, insurance, investTrust,
      bank, etcBank, pensionFund) <> (KospiData.tupled, KospiData.unapply)
  }

  val kospiQuery = TableQuery[CrawlKospiMapping]

  def findLatest(topN: Int) = kospiQuery.sortBy(_.targetDt.desc).drop(0).take(topN)

}
