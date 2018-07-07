package com.test.ecs

import com.test.ecs.util.KafkaUtils


import scala.collection.JavaConverters._

object EcsLoader {

  def main(args:Array[String])={
    val consumer=KafkaUtils.createConsumer("c","consumer2")
    while(true){
      val records=consumer.poll(10)
      records.asScala.foreach(record=>{
        println("value="+record.value())
      })
    }
  }
}
