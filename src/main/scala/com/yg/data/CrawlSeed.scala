package com.yg.data

import com.yg.conn.CrawlJobStatus

case class CrawlSeed(seedNo: Int, urlPattern: String, title: String, status: String)
case class CrawlSeedWithJob(seedNo: Int, urlPattern: String, title: String, status: String, crawlJobStatus: Array[CrawlJobStatus])

case class CrawlListWrapOption(
                                targetSeedUrl: String,
                                filterCrawlUrlRxPattern: String,
                                filterDomGroupAttr: String
                              )

case class CrawlListWrapOptionConf(
                                  crawlName: String,
                                targetSeedUrl: String,
                                filterCrawlUrlRxPattern: String,
                                filterDomGroupAttr: String
                              )

case class CrawlContentWrapOption(
                                   targetUrl: String,
                                   docTitle: String,
                                   docDatetime: String,
                                   contentGrp: String
                                 )

case class CrawlContentWrapOptionConf(
                                     seedNo: Int,
                                   targetUrl: String,
                                   docTitle: String,
                                   docDatetime: String,
                                   contentGrp: String
                                 )
