package com.test.ecs.util

import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import scala.collection.JavaConverters._

object KafkaUtils {

  def createConsumer(groupId:String, topicNameList:List[String]):KafkaConsumer[String,String]={
    val props = new Properties()
    EnvProperty.getEntriesWithPrefix("kafka.")
      .map(entry=>(entry._1.substring("kafka.".length),entry._2))
      .foreach(entry=>props.put(entry._1,entry._2))
    props.put("group.id", groupId)

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(topicNameList.asJava)
    consumer
  }

  def createProducer(): KafkaProducer[String, String] = {
    val props = new Properties()
    EnvProperty.getEntriesWithPrefix("kafka.")
      .map(entry=>(entry._1.substring("kafka.".length),entry._2))
      .foreach(entry=>props.put(entry._1,entry._2))
    new KafkaProducer[String, String](props);
  }
}
