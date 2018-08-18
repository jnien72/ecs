#!/bin/bash

cd `dirname "$0"`;
cd ..

export APP_HOME=`pwd`;

CLASSPATH="";

for jar_file in $APP_HOME/lib/*.jar; do
	CLASSPATH="$CLASSPATH:$jar_file";
done;

CLASSPATH="etc:$CLASSPATH";

JVM_OPTS="$JVM_OPTS -Dfile.encoding=UTF-8";
JVM_OPTS="$JVM_OPTS -verbose:gc";
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"
JVM_OPTS="$JVM_OPTS -XX:MaxGCPauseMillis=20"
JVM_OPTS="$JVM_OPTS -XX:InitiatingHeapOccupancyPercent=35"
JVM_OPTS="$JVM_OPTS -XX:+PrintGCDateStamps";
JVM_OPTS="$JVM_OPTS -XX:+PrintHeapAtGC";
JVM_OPTS="$JVM_OPTS -XX:+PrintGCDetails";
JVM_OPTS="$JVM_OPTS -XX:+PrintTenuringDistribution";
JVM_OPTS="$JVM_OPTS -XX:+UseGCLogFileRotation";
JVM_OPTS="$JVM_OPTS -XX:NumberOfGCLogFiles=1";
JVM_OPTS="$JVM_OPTS -XX:GCLogFileSize=1M";
JVM_OPTS="$JVM_OPTS -XX:+AlwaysPreTouch";
JVM_OPTS="$JVM_OPTS -XX:+UseCompressedOops";
JVM_OPTS="$JVM_OPTS -XX:+HeapDumpOnOutOfMemoryError";
JVM_OPTS="$JVM_OPTS -XX:HeapDumpPath=logs/loader-heap-dump.hprof";
JVM_OPTS="$JVM_OPTS -XX:ErrorFile=logs/loader-java-error.log";
JVM_OPTS="$JVM_OPTS -Xloggc:logs/server-gc.log";
JVM_OPTS="$JVM_OPTS -Xmx256m"
JVM_OPTS="$JVM_OPTS -Xms256m"

java -noverify -classpath $CLASSPATH $JVM_OPTS com.test.ecs.EcsServer