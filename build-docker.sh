#!/bin/bash

cd `dirname "$0"`;
BUILD_PATH=`pwd`;
BUILD_OUTPUT_PATH="$BUILD_PATH/dist/docker"

rm -Rf $BUILD_OUTPUT_PATH
mkdir -p $BUILD_OUTPUT_PATH;

# build ecs server

IMG_NAME="ecs-server"
FILE_PATH=$BUILD_OUTPUT_PATH/$IMG_NAME.img
cd $BUILD_PATH
sed 's/\[START_CMD\]/\/opt\/ecs\/sbin\/start-server.sh/g' \
 docker/dockerfile-template > dist/Dockerfile
cd dist
docker rmi $IMG_NAME 2> /dev/null | true
docker build -t $IMG_NAME .
echo "[ecs server] exporting image ... "
docker save $IMG_NAME | gzip > $FILE_PATH
echo "[ecs server] saved img to $FILE_PATH"

#build ecs loader

IMG_NAME="ecs-loader"
FILE_PATH=$BUILD_OUTPUT_PATH/$IMG_NAME.img
cd $BUILD_PATH
sed 's/\[START_CMD\]/\/opt\/ecs\/sbin\/start-loader.sh/g' \
 docker/dockerfile-template > dist/Dockerfile
cd dist
docker rmi $IMG_NAME 2> /dev/null | true
docker build -t $IMG_NAME .
echo "[ecs loader] exporting image ... "
docker save $IMG_NAME | gzip > $FILE_PATH
echo "[ecs loader] saved img to $FILE_PATH"