IMG_NAME="$1"

gunzip -c dist/docker/$IMG_NAME.img | docker load

docker stop $IMG_NAME
docker rm $IMG_NAME
docker run -d \
--name $IMG_NAME \
--network=host \
-h `hostname` \
-v /etc/timezone:/etc/timezone:ro \
-v /etc/localtime:/etc/localtime:ro \
-v /etc/hadoop/conf:/etc/hadoop/conf:ro \
$IMG_NAME