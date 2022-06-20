package com.yg.conn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import java.net.URLEncoder
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

trait CommonConn {
  implicit val system = ActorSystem()

  def writeData(rawData: String): String = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      //      uri = RuntimeConfig().getString("cpa_admin.cpa_api_write_url"),
      uri = "http://localhost:8086/write?db=mydb",
      entity = HttpEntity(
        rawData
      )
    )

    val res = Await.result(Http().singleRequest(request), 10.seconds)
    res.status.value
  }

}
