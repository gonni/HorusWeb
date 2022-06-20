package com.yg.conn

import akka.actor.ActorSystem

trait CommonConn {
  implicit val system = ActorSystem()



}
