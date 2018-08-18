package com.test.ecs.util

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName

class ParquetDriver(val feedName:String, val eventDate:String){

  private val tmpPath="/tmp/ecs-"+feedName+"_"+eventDate+"_"+System.currentTimeMillis()+".parquet"
  private val schemaStr=FeedUtils.getAvroSchema(feedName)
  private val schema = new Schema.Parser().parse(schemaStr)

  private var writer:AvroParquetWriter[GenericRecord]=null
  def getOrCreateWriter():AvroParquetWriter[GenericRecord]={
    this.synchronized{
      if(writer==null){
        writer=new AvroParquetWriter(
          new Path(tmpPath), schema,
          CompressionCodecName.SNAPPY,
          ParquetWriter.DEFAULT_BLOCK_SIZE,
          ParquetWriter.DEFAULT_PAGE_SIZE,
          false,
          new Configuration()
        )
      }
      writer
    }
  }

  def isWriterInitialized():Boolean=(writer!=null)

  def getSchema():Schema={
    schema
  }

  def getTmpPath():String={
    tmpPath
  }
}
