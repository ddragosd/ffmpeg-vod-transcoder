package org.streamkit.transcoding.job;

import org.springframework.batch.core.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by ddascal on 22/01/15.
 */
public class TranscodeVideo implements Step {
    private Logger logger = Logger.getLogger(ReadConfigJson.class.getName());

    @Override
    public String getName() {
        return "transcode-video";
    }

    @Override
    public boolean isAllowStartIfComplete() {
        return false;
    }

    @Override
    public int getStartLimit() {
        return 1;
    }

    @Override
    public void execute(StepExecution stepExecution) throws JobInterruptedException {
        logger.info("Transcoding video file");
        if (transcode()) {
            stepExecution.setStatus(BatchStatus.COMPLETED);
        } else {
            stepExecution.setStatus(BatchStatus.FAILED);
        }
    }

    /**
     * Starts a new FFmpeg Process as a separate process, not a separate thread and waits for the process to finish
     * before returning the result.
     *
     * FFmpeg output needs to be sent to the log console to be captured along with the other logs
     *
     * @return
     * @link http://docs.oracle.com/javase/7/docs/api/java/lang/ProcessBuilder.html
     */
    private Boolean transcode() throws JobInterruptedException {
        /*ProcessBuilder pb =
                new ProcessBuilder("ffmpeg", "-i", "myArg2");
        Map<String, String> env = pb.environment();
        env.put("VAR1", "myValue");
        env.remove("OTHERVAR");
        env.put("VAR2", env.get("VAR1") + "suffix");
        pb.directory(new File("myDir"));
        File log = new File("log");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));

        Process p = null;
        try {
            p = pb.start();
            assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
            assert pb.redirectOutput().file() == log;
            assert p.getInputStream().read() == -1;
        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        }*/

        return true;
    }


}
