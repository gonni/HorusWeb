package com.yg.conn
import akka.actor.ActorSystem
import com.yg.RuntimeConfig
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._
import org.slf4j.LoggerFactory

object NlpCoreClient extends BaseConn {
  val logger =  LoggerFactory.getLogger(getClass)

  override implicit val system: ActorSystem = ActorSystem()
  implicit val formats: DefaultFormats = DefaultFormats

  def getTopicScore(sentence: String) = {
    postJsBody[TopicScoreRequest](RuntimeConfig("horus.crawl") + "/nlp/topic/score",
      TopicScoreRequest(sentence))
  }

  def getTopicScoreJs(sentence: String) : TopicScoreResult = {
    val strJsRes = getTopicScore(sentence)
    read[TopicScoreResult](strJsRes)
  }

  def postJsBody[R <: AnyRef](url: String, request: R) : String = {
    val jsReqBody = write(request)
    logger.info(s"Request to ${url} with ${jsReqBody}")
    val resBody = writeDataAndRecv(url, jsReqBody)
    logger.info(s"Response Body : ${resBody}")
    resBody
  }

  def main(args: Array[String]): Unit = {
    println("Result =>" + getTopicScoreJs("전남도교육청, 코로나19 재확산에 ‘학원 방역 점검’"))
  }
}

case class TopicScoreRequest(sentence: String)
case class TopicScoreResult(sentence: String, tokens: List[String], termScores: List[TopicScore])
case class TopicScore(topic: String, score: Double)