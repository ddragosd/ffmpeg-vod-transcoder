# ffmpeg-vod-transcoder
A micro-service with Springboot and Docker used to transcode a single video file in multiple formats, in the cloud.

### Running the transcoding job in Chronos
This microservice is built to be executed once for every file that needs to be transcoded.
[Chronos](https://github.com/mesos/chronos) is a nice way to schedule jobs if you're using Mesos. Chronos is a fault tolerant distributed scheduler for jobs that includes ISO8601 support and dependency based job scheduling.

To start a job in Chronos make a `POST` request to `http://<master>:4400/scheduler/iso8601` with body:
 
```javascript
{ 
 "schedule": "1\//",
 "name": "transcoding-job-01",
 "container": {
   "type": "DOCKER",
   "image": "ddragosd/ffmpeg-vod-transcoder:latest",
   "network":"BRIDGE"
 },
 "cpus": "3",
 "mem": "2048",
 "uris": [],
 "arguments": "--configJson={\"source\":\"http://s3.footagesearch.com/demos/naturefootage/Q4/OF-04-Fish-Demo-Q4.mp4\"}"
}
```

### Developer guide

To build the microservice simply issue:

```
    make all
```

This Makefile command will build the java binary, updating the docker container making it ready for execution.


#### Running the transcoder in Docker

To test with Docker execute:

```
    make docker-run
```

To pass extra parameters to the docker container, use `DOCKER_ARGS`:

```
    make docker-run DOCKER_ARGS="--configJson='{\"source\":\"http://techslides.com/demos/sample-videos/small.mp4\"}'"
```

```
    make docker-run DOCKER_ARGS="--configJson='{\"source\":\"http://s3.footagesearch.com/demos/naturefootage/Q4/OF-04-Fish-Demo-Q4.mp4\"}'"
```

```
    make docker-run DOCKER_ARGS=\"--configJson='`cat ./src/main/resources/default_config_2.json`'\ --debug\"
```

The result of the encoding is saved into your home folder, under `~/tmp/streamkit/`.
To test with the same input file multiple times you might need to clean this directory; ffmpeg will not overwrite any files.