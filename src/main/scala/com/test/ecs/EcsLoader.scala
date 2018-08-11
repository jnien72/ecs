package com.test.ecs

import com.test.ecs.model.FeedField
import com.test.ecs.util._
import org.apache.avro.Schema
import org.apache.avro.Schema.Type
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import scalaz.std.java.enum

import scala.collection.JavaConverters._
import scala.collection.mutable

object EcsLoader {

  object EcsLoaderState extends Enumeration {
    val paused, running = Value
  }

  @volatile
  private var expectedState=EcsLoaderState.running
  @volatile
  private var currentState=EcsLoaderState.running


  case class ParquetDriver(feedName:String, eventDate:String,
                           tmpPath:String, schema:Schema,
                           writer:AvroParquetWriter[GenericRecord])

  private val currentDrivers=mutable.HashMap[(String,String),ParquetDriver]()

  def getParquetDriver(feedName:String, event: Map[String,Any]):ParquetDriver={
    val eventTs=event.get(Constants.EVENT_TIMESTAMP).get.toString.toLong
    val eventDate=DateTimeUtils.getDateTimeExp(eventTs,"yyyy-MM-dd")
    val driverId=(feedName,eventDate)
    this.synchronized{
      var currentDriver=currentDrivers.getOrElse(driverId,null)
      if(currentDriver==null){
        val tmpPath="/tmp/ecs-"+feedName+"_"+eventDate+"_"+System.currentTimeMillis()+".parquet"
        val avroSchemaStr=FeedUtils.getAvroSchema(feedName)
        val avroSchema = new Schema.Parser().parse(avroSchemaStr)
        val writer: AvroParquetWriter[GenericRecord] = new AvroParquetWriter(
          new Path(tmpPath), avroSchema,
          CompressionCodecName.SNAPPY,
          ParquetWriter.DEFAULT_BLOCK_SIZE,
          ParquetWriter.DEFAULT_PAGE_SIZE,
          false,
          new Configuration()
        )
        currentDriver=ParquetDriver(feedName, eventDate,tmpPath,avroSchema,writer)
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

  private var consumerThread:Thread=null
  def startConsumer():Unit={
    this.synchronized{
      if(consumerThread==null){
        consumerThread=new Thread(new Runnable(){
          override def run(): Unit = {
            val consumer=KafkaUtils.createConsumer(
              "consumer2", FeedUtils.getFeedList())

            while(true){
              if(expectedState!=currentState){
                currentState=expectedState
              }
              if(currentState==EcsLoaderState.running){
                val records=consumer.poll(100)
                records.asScala.foreach(record=>{
                  val json=record.value()
                  val jsonObj=JsonUtils.fromJson[Map[String,Any]](json)
                  val driver=getParquetDriver(record.topic(), jsonObj)
                  val parquetRecord=toRecord(jsonObj, driver.schema)
                  driver.writer.write(parquetRecord)
                  println("wrote "+ json+ " to "+ driver.feedName+" "+
                    driver.tmpPath)
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


  def startLoader():Unit={
    val intervalInMs=1000*60
    while(true){
      Thread.sleep(intervalInMs)
      // set expected state to paused
      // wait until current state is paused
      // for each writer, close them.
      // set expected state to running
      
    }
  }

  def main(args:Array[String])={
    startConsumer()
  }
}
