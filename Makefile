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
	docker run -ti --entrypoint='bash' streamkit/ffmpeg-vod-transcoder:latest

.PHONY: docker-run
docker-run:
	docker run streamkit/ffmpeg-vod-transcoder:latest ${DOCKER_ARGS}