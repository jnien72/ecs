name := "ecs"
version := "0.1"
scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.11.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.11.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25",
  "org.http4s" %% "http4s-blaze-server" % "0.15.4a",
  "org.http4s" %% "http4s-dsl"          % "0.15.4a",
  "org.http4s" %% "http4s-argonaut"     % "0.15.4a"
)

updateOptions := updateOptions.value.withCachedResolution(true)

libraryDependencies ~= {_
  .map(_.exclude("javax.jms", "jms"))
  .map(_.exclude("com.sun.jdmk", "jmxtools"))
  .map(_.exclude("com.sun.jmx", "jmxri"))
}

assemblyMergeStrategy in assembly := {
  case x if x.startsWith("META-INF") && x.endsWith("MANIFEST.MF") => MergeStrategy.discard
  case x => MergeStrategy.first
}

