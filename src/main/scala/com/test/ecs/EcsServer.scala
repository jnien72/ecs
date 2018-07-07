package com.test.ecs

import com.test.ecs.util.KafkaUtils
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.dsl._
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

object EcsServer {

  val LOG = LoggerFactory.getLogger(getClass())

  val whiteListTopics=Set[String]("a","b","c","d")

  val service = HttpService {
    case req @ POST -> Root / "topic" / topicName => {
      if(whiteListTopics.contains(topicName)){
        val is=scalaz.stream.io.toInputStream(req.body)
        val body=scala.io.Source.fromInputStream(is).mkString
        Ok()
      }else{
        NotFound()
      }
    }
  }

  def main(args:Array[String]):Unit={
    val consumer=KafkaUtils.createConsumer("c","qweofijewf")
    while(true){
      val records=consumer.poll(1000)
      records.asScala.foreach(record=>{
        println("value="+record.value())
      })
    }
    //BlazeBuilder.bindHttp(8080, "0.0.0.0")
    //  .mountService(service, "/").run.awaitShutdown()
  }
}
