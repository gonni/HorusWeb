package com.yg.conn

import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.ExecutionContext.Implicits.global

trait BaseConn {
  implicit val system : ActorSystem;

  def getData(url: String): String = {
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = url
    )

    val res = Await.result(Http().singleRequest(request), 10.seconds)
    val status = res.status.value
    if(status == "200 OK") {
      println("Dive into 200 OK")

      Await.result(Unmarshal(res.entity).to[String], Duration.Inf)
    } else {
      println("Detected Error ..")
      s"Error: ${status}"
    }
  }

  def writeData(url: String, bodyData: String): String = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, bodyData)
    )

    val res = Await.result(Http().singleRequest(request), 10.seconds)
    val status = res.status.value
    if(status == "200 OK") {
      println("Dive into 200 OK")
      val data = res.entity.dataBytes.map(_.utf8String).runForeach(data => println(data))
      "Succ"
    } else {
      println("Detected Error ..")
      s"Error: ${status}"
    }
  }

  def writeDataAndRecv(url: String, bodyData: String): String = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, bodyData)
    )

    val responseFuture = Http().singleRequest(request)
    val timeout = 3000.millis
    val responseAsString = Await.result(
      responseFuture
        .flatMap {resp => resp.entity.toStrict(timeout)}
        .map {strictEntity => strictEntity.data.utf8String},
      timeout
    )

    responseAsString
  }
}
