package com.test.ecs

import com.test.ecs.util._
import org.apache.avro.Schema
import org.apache.avro.Schema.Type
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

object EcsLoader {

  private val LOG = LoggerFactory.getLogger(getClass())

  def main(args:Array[String])={
    HadoopUtils.loadHadoopConf()
    startConsumer()
    startLoader()
  }

  private var consumerThread:Thread=null
  private def startConsumer():Unit={
    this.synchronized{
      if(consumerThread==null){
        consumerThread=new Thread(new Runnable(){
          override def run(): Unit = {
            val consumerId=EnvProperty.get(EnvProperty.LOADER_CONSUMER_GROUP)
            val consumer=KafkaUtils.createConsumer(
              consumerId, FeedUtils.getFeedList())
            while(true){
              if(expectedState!=currentState){
                currentState=expectedState
              }
              if(currentState==EcsLoaderState.running){
                val records=consumer.poll(100)
                records.asScala.foreach(record=>{
                  val json=record.value()
                  val feedName=record.topic()
                  try{
                    val jsonObj=JsonUtils.fromJson[Map[String,Any]](json)
                    val driver=getParquetDriver(record.topic(), jsonObj)
                    val parquetRecord=toRecord(jsonObj, driver.getSchema())
                    driver.getOrCreateWriter().write(parquetRecord)
                    LOG.info("[consumer] " + "add "+ json+ " to "+ feedName+" "+
                      driver.getTmpPath())
                  }catch{
                    case t:Throwable => {
                      LOG.error("[consumer] " + "error writing "+json+" to "+feedName, t)
                    }
                  }
                })
              }else{
                Thread.sleep(100)
              }
            }
          }
        })
        consumerThread.start()
      }
    }
  }

  private def startLoader():Unit={
    val intervalInSeconds=
      EnvProperty.get(EnvProperty.LOADER_INTERVAL_SECONDS).toInt
    val intervalInMs=
      DateTimeUtils.SECOND_IN_MILLIS * intervalInSeconds
    LOG.info("Waiting for data input")
    while(true){
      Thread.sleep(intervalInMs)
      expectedState=EcsLoaderState.paused
      while(expectedState!=currentState){
        Thread.sleep(100)
      }
      currentDrivers.values.foreach(driver=>{
        if(driver.isWriterInitialized()){
          driver.getOrCreateWriter().close()
          val outputParentPath=EnvProperty.get(EnvProperty.LOADER_OUTPUT_PATH)
          val outputPath=outputParentPath+"/"+driver.feedName+"/event_date="+driver.eventDate
          val outputFilePath=outputPath+"/"+System.currentTimeMillis()+".parquet"
          val fs=FileSystem.get(HadoopUtils.getHadoopConf())
          fs.mkdirs(new Path(outputPath))
          fs.rename(new Path(driver.getTmpPath()),new Path(outputFilePath))
          LOG.info("[loader] " + "Created data file for ["+driver.feedName+"] at "+outputFilePath)
        }
      })
      currentDrivers.clear()

      expectedState=EcsLoaderState.running
    }
  }

  private object EcsLoaderState extends Enumeration {
    val paused, running = Value
  }

  @volatile
  private var expectedState=EcsLoaderState.running

  @volatile
  private var currentState=expectedState

  private val currentDrivers=mutable.HashMap[(String,String),ParquetDriver]()

  private def getParquetDriver(feedName:String, event: Map[String,Any]):ParquetDriver={
    val eventTs=event.get(Constants.EVENT_TIMESTAMP).get.toString.toLong
    val eventDate=DateTimeUtils.getDateTimeExp(eventTs,"yyyy-MM-dd")
    val driverId=(feedName,eventDate)
    this.synchronized{
      var currentDriver=currentDrivers.getOrElse(driverId,null)
      if(currentDriver==null){
        currentDriver=new ParquetDriver(feedName, eventDate)
        currentDrivers.put(driverId, currentDriver)
      }
      currentDriver
    }
  }

  private def toRecord(input: Map[String, Any], schema: Schema): GenericRecord = {
    var recordBuilder = new GenericRecordBuilder(schema)
    input.foreach(field => {
      val key = field._1
      val value = field._2
      if (value.isInstanceOf[Map[Any, Any]]) {
        val fieldType = schema.getField(key).schema().getType
        if (fieldType == Type.UNION) {
          schema.getField(key).schema().getTypes().toArray.foreach(tmp => {
            val s = tmp.asInstanceOf[Schema]
            if (s.getType() == Type.RECORD) {
              val nextLevel = toRecord(value.asInstanceOf[Map[String, Any]], s)
              recordBuilder = recordBuilder.set(key, nextLevel)
            }
          })
        }
      } else {
        recordBuilder = recordBuilder.set(key, value)
      }
    })
    recordBuilder.build()
  }
}
