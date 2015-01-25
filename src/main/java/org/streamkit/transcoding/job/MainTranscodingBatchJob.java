package org.streamkit.transcoding.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * Created by ddascal on 22/01/15.
 */
@Configuration
@EnableBatchProcessing
public class MainTranscodingBatchJob  {
    @Autowired
    private JobBuilderFactory jobs;

    @Bean
    public Job job() {
        return jobs.get("transcoding-job")
                .start(new ReadConfigJson())
                .next(new TranscodeVideo())
                .next(new SnapshotVideo())
                .next(new PersistVideoS3())
                .listener(new TranscodingListener())
                .build();
    }

    @Bean
    public ObjectMapper getJsonReader() {
        return new ObjectMapper();
    }

    /*@Bean
    protected Step readConfig() {
        return new ReadConfigJson();
    }*/

}
