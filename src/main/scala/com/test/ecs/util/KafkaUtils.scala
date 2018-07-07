package com.test.ecs.util

import java.util.Properties
import java.util.Collections

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

object KafkaUtils {

  def createConsumer(topicName:String, groupId:String):KafkaConsumer[String,String]={
    val props = new Properties()
    props.put("bootstrap.servers","localhost:9092")
    props.put("key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("group.id", groupId)
    props.put("auto.offset.reset","latest")

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(Collections.singletonList(topicName))
    consumer
  }

  def createProducer(): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("acks","1")
    props.put("retries","10")
    props.put("batch.size","16384")
    props.put("linger.ms","100")
    props.put("buffer.memory","33554432")
    props.put("key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer")
    new KafkaProducer[String, String](props);
  }
}
