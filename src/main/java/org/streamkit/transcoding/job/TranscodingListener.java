package org.streamkit.transcoding.job;

import org.springframework.batch.core.*;

import java.util.logging.Logger;

/**
 * Created by ddascal on 22/01/15.
 */
public class TranscodingListener implements JobExecutionListener {
    Logger logger = Logger.getLogger(TranscodingListener.class.getName());

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("beforeJob()");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.info("afterJob(): TODO: MAKE THE HTTP CALLBACK");
    }
}
