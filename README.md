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
    make docker-run DOCKER_ARGS="--configJson='{\"source\":\"http://techslides.com/demos/sample-videos/small.mp4\"}'"
```

The result of the encoding is saved into your home folder, under `~/tmp/streamkit/`.
To test with the same input file multiple times you might need to clean this directory; ffmpeg will not overwrite any files.