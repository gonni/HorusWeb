package com.yg.vs1

object PreVsDef {
  implicit class ExString(val src: String) {
    def display = {
      println("Implicited Value : " + src)
    }
  }
}
