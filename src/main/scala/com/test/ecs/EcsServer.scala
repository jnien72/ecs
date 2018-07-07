package com.test.ecs

import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.dsl._

object EcsServer {

  val service = HttpService {
    case GET -> Root / "hello" / name => {
      Ok(s"hello ${name}!")
    }
    case req @ POST -> Root / "echo" => {
      Ok(req.body)
    }
  }

  def main(args:Array[String]):Unit={
    BlazeBuilder.bindHttp(8080, "0.0.0.0")
      .mountService(service, "/").run.awaitShutdown()
  }
}
