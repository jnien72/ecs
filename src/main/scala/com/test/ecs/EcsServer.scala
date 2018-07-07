package com.test.ecs

import com.test.ecs.util.KafkaUtils
import org.apache.kafka.clients.producer.ProducerRecord
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.dsl._
import org.slf4j.LoggerFactory

object EcsServer {

  val LOG = LoggerFactory.getLogger(getClass())

  val whiteListTopics=Set[String]("a","b","c","d")

  val kafkaProducer=KafkaUtils.createProducer()

  val service = HttpService {

    case req @ POST -> Root / "topic" / topicName => {
      if(whiteListTopics.contains(topicName)){
        val is=scalaz.stream.io.toInputStream(req.body)
        val body=scala.io.Source.fromInputStream(is).mkString
        val record=new ProducerRecord[String,String](topicName,0,"",body)
        kafkaProducer.send(record)
        Ok()
      }else{
        NotFound()
      }
    }
  }

  def main(args:Array[String]):Unit={
    BlazeBuilder.bindHttp(8080, "0.0.0.0")
      .mountService(service, "/").run.awaitShutdown()
  }
}
