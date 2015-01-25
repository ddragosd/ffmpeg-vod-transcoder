package org.streamkit.transcoding.job;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;

import java.util.logging.Logger;

/**
 * Created by ddascal on 22/01/15.
 */
public class SnapshotVideo implements Step {
    Logger logger = Logger.getLogger(SnapshotVideo.class.getName());

    @Override
    public String getName() {
        return "take-video-snapshot";
    }

    @Override
    public boolean isAllowStartIfComplete() {
        return true;
    }

    @Override
    public int getStartLimit() {
        return 3;
    }

    @Override
    public void execute(StepExecution stepExecution) throws JobInterruptedException {
        logger.info("Taking snapshot");
        stepExecution.setStatus(BatchStatus.COMPLETED);
    }
}
