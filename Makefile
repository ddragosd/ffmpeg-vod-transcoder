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
	docker run --rm=true --volume=/tmp/streamkit:/tmp/streamkit -ti --entrypoint='bash' streamkit/ffmpeg-vod-transcoder:latest

.PHONY: docker-run
docker-run:
	mkdir -p ~/tmp/streamkit
	docker run --rm=true --volume=${HOME}/tmp/streamkit:/tmp/streamkit streamkit/ffmpeg-vod-transcoder:latest ${DOCKER_ARGS}
