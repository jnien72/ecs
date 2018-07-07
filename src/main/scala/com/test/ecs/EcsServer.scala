package com.test.ecs

import com.test.ecs.util.JsonUtils
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.dsl._
import org.slf4j.LoggerFactory

object EcsServer {

  val LOG = LoggerFactory.getLogger(getClass())

  val service = HttpService {
    case req @ POST -> Root / "topic" / topicName => {
      val is=scalaz.stream.io.toInputStream(req.body)
      val body=scala.io.Source.fromInputStream(is).mkString
      JsonUtils.fromJson[]()
      LOG.info("I got a message, to topic "+ topicName +" =>" +body)

      Ok("OK")
    }
  }

  def main(args:Array[String]):Unit={
    BlazeBuilder.bindHttp(8080, "0.0.0.0")
      .mountService(service, "/").run.awaitShutdown()
  }
}
