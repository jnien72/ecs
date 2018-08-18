package com.test.ecs.util

import com.typesafe.config.ConfigFactory

import scala.io.Source

object EnvProperty {

  val HTTP_PORT = "http.port"
  val HADOOP_CONF_DIR = "hadoop.conf.dir"
  val LOADER_CONSUMER_GROUP="loader.consumer.group"
  val LOADER_INTERVAL_SECONDS="loader.interval.seconds"

  def get(key:String):String={
    config.getString(key)
  }

  def getEntriesWithPrefix(prefix: String): Map[String, String] = {
    Source.fromURL(getClass.getResource("/"+resourceBaseName)).getLines()
      .map(x => x.trim).filter(x => (!x.startsWith("#"))).map(
      x => (if (x.indexOf("=") == x.lastIndexOf("=")) {
        val key = x.split("=")(0)
        try {
          (key, resolveConfig(key))
        } catch {
          case x: Throwable => (key, null)
        }
      } else (x, null))
    ).toMap.filter(x => (x._2 != null && x._1.startsWith(prefix)))
  }

  private val resourceBaseName="config.conf"
  private val config = ConfigFactory.load(resourceBaseName)

  private def resolveConfig(key: String): String = {
    var result = config.getString(key)
    while (result.contains("${")) {
      val subsKey = result.substring(result.indexOf("${") + 2, result.indexOf("}"))
      result = result.replace("${" + subsKey + "}", config.getString(subsKey))
    }
    result
  }
}
