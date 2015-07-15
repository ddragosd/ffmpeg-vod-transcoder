package org.streamkit.transcoding.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.PollableChannel;

import java.text.SimpleDateFormat;

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
                .next(getPersistVideoStep())
                .next(getPersistVideoFtpStep())
                .listener(getTranscodingListener())
                .build();
    }

    @Bean
    protected TranscodingListener getTranscodingListener() {
        return new TranscodingListener();
    }

    @Bean
    protected PersistVideoS3 getPersistVideoStep() {
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

    @Bean
    public PollableChannel remoteFileOutputChannel() {
        return new QueueChannel();
    }

}
