package com.yg.conn

import com.yg.data.{CrawlContentWrapOption, CrawlListWrapOption}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._
import org.slf4j.LoggerFactory

object CrawlCoreClient {
  val logger =  LoggerFactory.getLogger(getClass)
  implicit val formats: DefaultFormats = DefaultFormats

  def crawlListUrl(crawlOption : CrawlListWrapOption) : String = {
    val jsBody = write(crawlOption)
    logger.info(s"ListPage body data to send -> ${jsBody}")
    val response = CommonConn.writeData2("http://localhost:8070/crawl/page/list", jsBody)

    logger.info(s"Response Body -> ${response}")
    response
  }

  def crawlContentPage(crawlOption: CrawlContentWrapOption) = {
    val jsBody = write(crawlOption)
    logger.info(s"ContentPage body data to send -> ${jsBody}")
    val response = CommonConn.writeData2("http://localhost:8070/crawl/page/content", jsBody)

    logger.info(s"Response Body -> ${response}")
    response
  }

  def crawlJobStatus(seedNo: Int) = {
    val strResponse = CommonConn.getData("http://localhost:8070/crawl/admin/status/all?seedNo=" + seedNo)
//    val strResponse = CommonConn.getData("http://192.168.35.123:8070/crawl/admin/status/all?seedNo=" + seedNo)
    println("res => " + strResponse)
    read[Array[CrawlJobStatus]](strResponse)
  }

  def startCrawlJob(seedNo: Int) = {
    val strResponse = CommonConn.getData("http://localhost:8070/crawl/rt/schedule?seedNo=" + seedNo)
    logger.info("Response message for startCrawl : {}", strResponse);
    strResponse
  }

  def stopCrawlJob(seedNo: Int) = {
    val strResponse = CommonConn.getData("http://localhost:8070/crawl/rt/schedule/stop?seedNo=" + seedNo)
    logger.info("Response message for stopCrawl : {}", strResponse)
    strResponse
  }

  def sentenceTopicScore(sentence: String) = {
    val jsBody = write(TopicScoreRequest(sentence))
    logger.info(s"Sentence body for topic score -> ${jsBody}");
    val response = CommonConn.writeData2("http://localhost:8070/nlp/topic/score", jsBody)

    logger.info(s"Response Body -> ${response}");
    response
  }

  def main(args: Array[String]): Unit = {
//    sentenceTopicScore("우크라 곡물 첫 선적 완료…금명간 수출 개시 예상")
    println("json response =>")
    crawlJobStatus(21).foreach(println)
  }

}
case class CrawlJobStatus(
                         docType: String,
                         crawlStatus: String,
                         seedNo: Int,
                         consumer: Consumer,
                         cntJobProcessed: Int,
                         cntJobFetched: Int
                         )

case class Consumer(
                      cntWorkers: Int,
                      cntAlives: Int,
                      cntPendings: Int
                    )

case class TopicScoreRequest(
                            sentence: String
                            )

