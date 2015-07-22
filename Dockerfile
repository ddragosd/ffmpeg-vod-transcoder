# ffmpeg vod transcoder
#
# VERSION               2.4.3
#
# From https://trac.ffmpeg.org/wiki/CompilationGuide/Centos
#
FROM          jrottenberg/ffmpeg:2.4.3
MAINTAINER    Dragos Dascalita Haut <ddragosd@gmail.com>

ENV	LANG en_US.UTF-8
ENV	LC_ALL en_US.UTF-8

RUN yum install -y curl; yum upgrade -y; yum update -y;  yum clean all

ENV JDK_VERSION 8u31
ENV JDK_BUILD_VERSION b13

RUN curl -LO "http://download.oracle.com/otn-pub/java/jdk/$JDK_VERSION-$JDK_BUILD_VERSION/jdk-$JDK_VERSION-linux-x64.rpm" -H 'Cookie: oraclelicense=accept-securebackup-cookie' && rpm -i jdk-$JDK_VERSION-linux-x64.rpm; rm -f jdk-$JDK_VERSION-linux-x64.rpm; yum clean all

ENV JAVA_HOME /usr/java/default

# forward request and error logs to docker log collector
RUN mkdir -p /var/log/streamkit/
RUN ln -sf /dev/stdout /var/log/streamkit/*

# dir to write the logs into
VOLUME /var/log/streamkit/

# dir to write the transcoding output into
VOLUME /tmp/streamkit

RUN cd /tmp/ && curl -LO "https://github.com/streamkit/ffmpeg-vod-transcoder/releases/download/transcoding-job-0.0.6/transcoding-job-0.0.6.jar" && mkdir -p /usr/local/vod-transcoder/ && mv transcoding-job*.jar /usr/local/vod-transcoder/vod-transcoder.jar

# this volume is used for local development
VOLUME /usr/local/vod-transcoder

ENTRYPOINT ["java", "-jar", "/usr/local/vod-transcoder/vod-transcoder.jar"]
