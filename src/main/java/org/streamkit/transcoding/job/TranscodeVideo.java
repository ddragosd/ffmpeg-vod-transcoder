package org.streamkit.transcoding.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;
import org.streamkit.transcoding.model.TranscodingModel;
import org.streamkit.transcoding.model.VideoOutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by ddascal on 22/01/15.
 */
@Component
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
        TranscodingModel model = (TranscodingModel) stepExecution.getJobExecution().getExecutionContext().get("model");
        if (transcode(model)) {
            stepExecution.setStatus(BatchStatus.COMPLETED);
        } else {
            stepExecution.setStatus(BatchStatus.FAILED);
        }
    }

    /**
     * Starts a new FFmpeg Process as a separate process, not a separate thread and waits for the process to finish
     * before returning the result.
     * <p/>
     * FFmpeg output needs to be sent to the log console to be captured along with the other logs
     *
     * @return
     * @link http://docs.oracle.com/javase/7/docs/api/java/lang/ProcessBuilder.html
     */
    protected Boolean transcode(TranscodingModel model) throws JobInterruptedException {
        this.reduceConfigToVideoMetadata(model);

        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.info("Using final configuration:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // TODO: create final ffmpeg command based on the updated model
        // TODO: use java 8 stream to reduce the model to a String which is passed to the ffmpeg command
        // see: http://docs.oracle.com/javase/tutorial/collections/streams/parallelism.html
        ProcessBuilder pb =
                new ProcessBuilder("ffmpeg", "-i", model.getSource())
                        .inheritIO();
//        pb.directory(new File("myDir"));
//        File log = new File("/var/log/streamkit/ffmpeg-output.log");
//        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
//        pb.redirectErrorStream(true);
//        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);

        Process p = null;
        try {
            p = pb.start();
//            assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
////            assert pb.redirectOutput().file() == log;
//            assert pb.redirectOutput() == ProcessBuilder.Redirect.PIPE;
//            assert p.getInputStream().read() == -1;
        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        }

        return true;
    }

    protected void reduceConfigToVideoMetadata(TranscodingModel model) throws JobInterruptedException {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", model.getSource());
        Process p = null;
        try {
            p = pb.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.getProperty("line.separator"));
            }
            String ffmpegOutput = stringBuilder.toString();
            double[] info = extractWidthHeightBitrate(ffmpegOutput);
            logger.info(String.format("Video Info:\n%s\n\n", ffmpegOutput));
            if (info == null) {
                throw new JobInterruptedException("Could not read video info.");
            }

            double width = info[0];
            double height = info[1];
            double bitrate = info[2];

            //1. determine if this is an HD stream or not by the ratio
            boolean isHD = width / height >= 1.60D;
            logger.info(String.format("Width=%.2f, Height=%.2f, Bitrate=%.2f, HD=%b", width, height, bitrate, isHD));

            //2. reduce the config to the size of the video, stripping the high qualities
            if (isHD) {
                List<VideoOutput> outputs = model.getHd_outputs();
                VideoOutput[] filteredOutputs = outputs
                        .stream()
                        .filter(o -> o.getWidth() <= width)
                        .toArray(VideoOutput[]::new);
                List<VideoOutput> o = new ArrayList<VideoOutput>(Arrays.asList(filteredOutputs));
                model.setHd_outputs(o);
                model.setSd_outputs(null);
            } else {
                List<VideoOutput> outputs = model.getSd_outputs();
                VideoOutput[] filteredOutputs = outputs
                        .stream()
                        .filter(o -> o.getWidth() <= width)
                        .toArray(VideoOutput[]::new);
                List<VideoOutput> o = new ArrayList<VideoOutput>(Arrays.asList(filteredOutputs));
                model.setSd_outputs(o);
                model.setHd_outputs(null);
            }
        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        }
    }

    /**
     * returns an array containing [width, height, bitrate]
     *
     * @param ffmpegOutput
     * @return
     */
    private double[] extractWidthHeightBitrate(String ffmpegOutput) {
        Pattern p = Pattern.compile("^.*Stream.*Video.*\\s(?<width>\\d+)x(?<height>\\d+).*\\s(?<bitrate>\\d+)\\skb\\/s.*$", Pattern.MULTILINE);
        Matcher m = p.matcher(ffmpegOutput);
        if (m.find()) {
            return new double[]{Double.valueOf(m.group("width")), Double.valueOf(m.group("height")), Double.valueOf(m.group("bitrate"))};
        }
        return null;
    }


}
