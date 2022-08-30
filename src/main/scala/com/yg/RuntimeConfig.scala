package com.yg

import com.typesafe.config.ConfigFactory

object RuntimeConfig {
  val conf = ConfigFactory.load("application.conf")

  def getActiveConfig() = {
    conf.getConfig(conf.getString("profile.active"))
  }

  def getActiveProfile() = {
    conf.getString("profile.active")
  }

  def apply() = getActiveConfig()

  def apply(key: String) = getActiveConfig().getString(key)

  def main(args: Array[String]): Unit = {
    val aConf = conf.getString("profile.active")
    println("Conf -> " + aConf)
  }
}
