.PHONY: clean
clean:
	mvn clean

all: clean java docker

java:
	mvn install

docker:
	docker build -t streamkit/ffmpeg-vod-transcoder .

.PHONY: docker-ssh
docker-ssh:
	mkdir -p ~/tmp/docker/ffmpeg-vod-transcoder; cp ./target/transcoding-job*.jar ~/tmp/docker/ffmpeg-vod-transcoder/vod-transcoder.jar
	docker run --rm=true --volume=/tmp/streamkit:/tmp/streamkit --volume=${HOME}/tmp/docker/ffmpeg-vod-transcoder/:/usr/local/vod-transcoder -ti --entrypoint='bash' streamkit/ffmpeg-vod-transcoder:latest

.PHONY: docker-run
docker-run:
	mkdir -p ~/tmp/streamkit
	mkdir -p ~/tmp/docker/ffmpeg-vod-transcoder; cp ./target/transcoding-job*.jar ~/tmp/docker/ffmpeg-vod-transcoder/vod-transcoder.jar
	docker run --rm=true --volume=${HOME}/tmp/streamkit:/tmp/streamkit --volume=${HOME}/tmp/docker/ffmpeg-vod-transcoder/:/usr/local/vod-transcoder streamkit/ffmpeg-vod-transcoder:latest ${DOCKER_ARGS}
