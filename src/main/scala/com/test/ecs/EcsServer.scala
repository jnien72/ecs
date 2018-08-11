package com.test.ecs

import com.test.ecs.util.{Constants, FeedUtils, JsonUtils, KafkaUtils}
import org.apache.kafka.clients.producer.ProducerRecord
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.dsl._
import org.slf4j.LoggerFactory

import scala.collection.mutable

object EcsServer {

  val LOG = LoggerFactory.getLogger(getClass())

  val feedSet=FeedUtils.getFeedList().toSet
  val kafkaProducer=KafkaUtils.createProducer()

  val service = HttpService {

    case req @ POST -> Root / "feed" / feedName => {
      if(feedSet.contains(feedName)){
        val is=scalaz.stream.io.toInputStream(req.body)
        val json=scala.io.Source.fromInputStream(is).mkString
        try{
          val jsonObj=JsonUtils.fromJson[mutable.LinkedHashMap[String,Any]](json)
          jsonObj.put(Constants.EVENT_TIMESTAMP,System.currentTimeMillis())
          val parsedJson=JsonUtils.toJson(jsonObj)
          val record=new ProducerRecord[String,String](feedName,0,"",parsedJson)
          kafkaProducer.send(record)
          Ok()
        }catch {
          case t:Throwable => BadRequest(t.getMessage)
        }
      }else{
        NotFound()
      }
    }
  }

  def main(args:Array[String]):Unit={
    val port=8080
    val server=BlazeBuilder.bindHttp(port, "0.0.0.0")
      .mountService(service, "/").run
    LOG.info("Server started on port "+port)
    server.awaitShutdown()
  }
}
