package com.yg.api

import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

class RestSampleController extends ScalatraServlet with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  before() {
    contentType = formats("json")
  }

  get("/"){
    FlowerData.all
  }

  post("/create") {
    val res = parsedBody.extract[Person]
    println(s"${res.id} --> ${res.name}")

    Person(13, "RandomName_" + Math.random())
  }
}

case class Person(id: Int, name: String)

case class Flower(slug: String, name: String)

object FlowerData {

  /**
   * Some fake flowers data so we can simulate retrievals.
   */
  var all = List(
    Flower("yellow-tulip", "Yellow Tulip"),
    Flower("red-rose", "Red Rose"),
    Flower("black-rose", "Black Rose"))
}