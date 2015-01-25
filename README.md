# ffmpeg-vod-transcoder
A micro-service with Springboot and Docker used to transcode a single video file in multiple formats, in the cloud.

### Developer guide

To build the microservice simply issue:

```
    make all
```

This Makefile command will build the java binary, updating the docker container making it ready for execution.


#### Running the microservice in Docker

To test with Docker execute:

```
    make docker-run
```

To pass extra parameters to the docker container, use `DOCKER_ARGS`:

```
    make docker-run DOCKER_ARGS=--user-config="{'input':'http://test-url'}"
```