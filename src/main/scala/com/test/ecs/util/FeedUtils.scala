package com.test.ecs.util

import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.test.ecs.model.FeedField
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object FeedUtils {

  private val LOG = LoggerFactory.getLogger(getClass())

  def getFeedList():List[String]={
    val stream : InputStream = getClass.getResourceAsStream("/schema.lst")
    scala.io.Source.fromInputStream( stream ).getLines.toList
  }


  def getFields(feed:String):List[FeedField]={
    val stream : InputStream = getClass.getResourceAsStream("/schema/"+feed+".json")
    val jsonContent=scala.io.Source.fromInputStream( stream ).getLines.mkString("\n")
    JsonUtils.fromJson[List[FeedField]](jsonContent)
  }

  def getAvroSchema(feed:String):String={
    val fields=getFields(feed)
    val base = mutable.LinkedHashMap[String, Any]()
    var outputFields = ArrayBuffer[mutable.LinkedHashMap[String, Any]]()
    base.put("name", feed)
    base.put("type", "record")
    base.put("fields", outputFields)

    val subtreeMapping = mutable.LinkedHashMap[String, mutable.LinkedHashMap[String, Any]]()
    subtreeMapping.put("", base)

    fields.foreach(field => {
      val path = field.name
      var parentPath = ""
      val tokens = path.split("\\.")
      val fieldType = field.fieldType
      tokens.foreach(token => {
        val name = token
        val isLeaf = if (parentPath.length > 0) {
          ((parentPath + "." + name).equals(path))
        } else {
          (name.equals(path))
        }
        var parentObj: mutable.LinkedHashMap[String, Any] = subtreeMapping.get(parentPath).getOrElse(null)

        if (parentObj == null) {
          parentObj = mutable.LinkedHashMap[String, Any]()
          parentObj.put("name", parentPath.substring(parentPath.lastIndexOf(".")+1))
          parentObj.put("type", "record")
          parentObj.put("fields", ArrayBuffer[mutable.LinkedHashMap[String, Any]]())
          subtreeMapping.put(parentPath, parentObj)

          val parentContainer = mutable.LinkedHashMap[String, Any]()
          parentContainer.put("name", parentPath.substring(parentPath.lastIndexOf(".")+1))
          parentContainer.put("default", null)
          parentContainer.put("type", ArrayBuffer[Any]("null", parentObj))
          outputFields.append(parentContainer)
        }

        outputFields = parentObj.get("fields").get.asInstanceOf[ArrayBuffer[mutable.LinkedHashMap[String, Any]]]

        if (isLeaf) {
          outputFields.append(mutable.LinkedHashMap[String, Any](("name", name), ("default",null),("type", Array[Any]("null",fieldType))))
        }

        if (parentPath.length == 0) {
          parentPath = name
        } else {
          parentPath = parentPath + "." + name
        }
      })
    })
    JsonUtils.toJson(base,true)
  }

//  def getEcsSchema(feed:String):(mutable.LinkedHashMap[String,Any])={
//    val fieldList:List[FeedField]=getFields(feed)
//    val schema=mutable.LinkedHashMap[String,Any]()
//    fieldList.foreach(field=>{
//      val name=field.name
//      val path=name.split("\\.")
//      var parent=schema
//      for(i<-0 to path.length-1){
//        val name=path(i)
//        val simpleName=if(name.contains(".")){
//          name.substring(name.lastIndexOf(".")+1)
//        }else{
//          name
//        }
//        validateFieldName(name)
//        if(i<path.length-1){
//          parent.getOrElseUpdate(name,mutable.LinkedHashMap[String,Any]())
//          parent=parent.get(name).get.asInstanceOf[mutable.LinkedHashMap[String,Any]]
//        }else{
//          if(!parent.put(simpleName,field).isEmpty){
//            throw new RuntimeException("'"+field.name+"' has multiple definitions")
//          }
//        }
//      }
//    })
//    schema
//  }
//
//
//  private val validFieldNameChars="0123456789abcdefghijklmnopqrstuvwxyz_."
//
//  def validateFieldName(name:String):Unit={
//    if(name ==null){
//      throw new RuntimeException("Unspecified feed_list name")
//    }
//    if(name.length<1 || name.length>128){
//      throw new RuntimeException("Feed name length must be between 1 and 128 characters")
//    }
//    name.foreach(x=>{
//      if(validFieldNameChars.indexOf(x)<0){
//        throw new RuntimeException("Feed name '"+name+"' contains invalid chars, valid chars are [a-z] or [0-9] or '_'")
//      }
//    })
//    if(name.charAt(0)<'a'&&name.charAt(0)>'z'){
//      throw new RuntimeException("Feed name '"+name+"' must start with [a-z]")
//    }
//  }
}
