package com.test.ecs.util

import java.util.Properties
import java.util.Collections
import org.apache.kafka.clients.consumer.KafkaConsumer

object KafkaUtils {

  def createConsumer(topicName:String, groupId:String):KafkaConsumer[String,String]={
    val props = new Properties()
    props.put("bootstrap.servers","localhost:9092")
    props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset","latest")
    props.put("group.id", groupId)

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(Collections.singletonList(topicName))
    consumer
  }
}
