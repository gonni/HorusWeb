package com.yg.api

import org.scalatra._

class RestSampleController extends ScalatraServlet {
  get("/"){
    FlowerData.all
  }
}

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