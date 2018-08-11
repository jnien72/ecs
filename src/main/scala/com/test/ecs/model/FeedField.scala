package com.test.ecs.model

import com.fasterxml.jackson.annotation.JsonProperty

case class FeedField (
   @JsonProperty("name") name:String,
   @JsonProperty("type") fieldType:String,
   @JsonProperty("doc") doc:String
)