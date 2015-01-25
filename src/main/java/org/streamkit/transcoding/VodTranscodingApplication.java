package org.streamkit.transcoding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VodTranscodingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VodTranscodingApplication.class, args);

        // TODO: make sure you read command line args --configJson={...json...}
        // TODO: parse config in the Model

        // http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-external-config-command-line-args
    }
}
