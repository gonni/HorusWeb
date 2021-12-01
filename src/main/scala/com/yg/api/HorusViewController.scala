package com.yg.api

import com.yg.data.CrawlSeed
import org.scalatra._

class HorusViewController extends ScalatraServlet {

  get("/seeds") {
    val data = List(
      CrawlSeed(1L, "Https://www.naver.com", "Naver",  "Good"),
      CrawlSeed(2L, "Https://www.dakao.com", "Daum",  "Good"),
      CrawlSeed(3L, "Https://www.nate.com", "Nate", "Bad"),
      )
//      com.yg.api.html
      com.yg.api.html.horus.render(data)
  }


}
