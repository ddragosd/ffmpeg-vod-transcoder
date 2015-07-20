package org.streamkit.transcoding.job;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ddascal on 22/01/15.
 */
@Configuration
@EnableBatchProcessing
public class MainTranscodingBatchJob {
    @Autowired
    private JobBuilderFactory jobs;

    @Bean
    public Job job() {
        return jobs.get("transcoding-job")
                .start(getReadConfigJob())
                .next(getTranscodeStep())
                .next(getSnapshotStep())
                .next(getPersistVideoS3Step())
                .next(getPersistVideoFtpStep())
                .listener(getTranscodingListener())
                .build();
    }

    @Bean
    protected TranscodingListener getTranscodingListener() {
        return new TranscodingListener();
    }

    @Bean
    protected PersistVideoS3 getPersistVideoS3Step() {
        return new PersistVideoS3();
    }

    @Bean
    protected PersistVideoFTP getPersistVideoFtpStep() {
        return new PersistVideoFTP();
    }

    @Bean
    protected SnapshotVideo getSnapshotStep() {
        return new SnapshotVideo();
    }

    @Bean
    protected ReadConfigJson getReadConfigJob() {
        return new ReadConfigJson();
    }

    @Bean
    protected TranscodeVideo getTranscodeStep() {
        return new TranscodeVideo();
    }


}
