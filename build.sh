#!/bin/bash

project_name="ecs"

cd `dirname "$0"`;
BUILD_PATH=`pwd`;
OUTPUT_PATH="${BUILD_PATH}/dist"
OUTPUT_RUNTIME_PATH="${OUTPUT_PATH}/${project_name}"

cd ${BUILD_PATH}
rm -rf ${OUTPUT_PATH}
sbt clean assembly

BUILD_EXIT_CODE="$?";
if [ "$BUILD_EXIT_CODE" != "0" ]; then
    echo "Build Finished with error(s)"
    exit 1;
fi;

mkdir -p ${OUTPUT_RUNTIME_PATH}

mkdir ${OUTPUT_RUNTIME_PATH}/etc

for f in ${BUILD_PATH}/src/main/resources/*; do
	cp -r $f ${OUTPUT_RUNTIME_PATH}/etc >> /dev/null 2>&1
done

cp -r ${BUILD_PATH}/sbin ${OUTPUT_RUNTIME_PATH}/sbin
chmod 755 ${OUTPUT_RUNTIME_PATH}/sbin/*

mkdir -p ${OUTPUT_RUNTIME_PATH}/lib
cp target/scala-*/*.jar ${OUTPUT_RUNTIME_PATH}/lib

mkdir -p ${OUTPUT_RUNTIME_PATH}/logs

echo
echo "Build finished successfully => ${OUTPUT_PATH}"
