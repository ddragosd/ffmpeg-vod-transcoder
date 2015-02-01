package org.streamkit.transcoding.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;
import org.streamkit.transcoding.model.TranscodingModel;
import org.streamkit.transcoding.model.VideoOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        VideoInputMetadata inputMeta = this.getFFmpegMediaParameters(model);
        TranscodingModel reducedModel = this.reduceConfigToVideoMetadata(inputMeta, model);

        if (transcode(reducedModel)) {
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
    protected Boolean transcode(TranscodingModel reducedModel) throws JobInterruptedException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.info("Using final configuration:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reducedModel));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        List<VideoOutput> outputs = reducedModel.getHd_outputs() != null ? reducedModel.getHd_outputs() : reducedModel.getSd_outputs();

        String fileName = this.extractFileName(reducedModel.getSource());
        StringBuilder ffmpegCmdBuilder = new StringBuilder();
        ffmpegCmdBuilder.append("ffmpeg -i ")
                .append(reducedModel.getSource())
                .append(" ")
                .append(outputs.stream()
                        .map(o -> String.format("-f mp4 -c:a copy -c:v %s -s %dx%d -x264opts bitrate=%d %s ",
                                        o.getVideo_codec(), o.getWidth(), o.getHeight(), o.getBitrate(),
                                        reducedModel.getDestination().getFile_name_template()
                                                .replaceAll("\\$width", String.valueOf(o.getWidth()))
                                                .replaceAll("\\$height", String.valueOf(o.getHeight()))
                                                .replaceAll("\\$bitrate", String.valueOf(o.getBitrate()))
                                                .replaceAll("\\$originalFileName", fileName) + ".mp4")
                        ).reduce("", (a, b) -> a + b)
                );
        String[] commands = ffmpegCmdBuilder.toString().split(" ");
        // TODO: include trim
        // see: http://docs.oracle.com/javase/tutorial/collections/streams/parallelism.html
        ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
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

    protected VideoInputMetadata getFFmpegMediaParameters(TranscodingModel model) throws JobInterruptedException {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", model.getSource());
        Process p;
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
            VideoInputMetadata fOut = extractWidthHeightBitrate(ffmpegOutput);
            logger.info(String.format("Video Info:\n%s\n\n", ffmpegOutput));
            if (fOut == null) {
                throw new JobInterruptedException("Could not read video info.");
            }

            return fOut;

        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        }
    }


    protected TranscodingModel reduceConfigToVideoMetadata(VideoInputMetadata fOut, TranscodingModel model) {

        //1. determine if this is an HD stream or not by the ratio
        boolean isHD = fOut.width / fOut.height >= 1.60D;
        logger.info(String.format("Width=%.2f, Height=%.2f, Bitrate=%.2f, HD=%b",
                fOut.width, fOut.height, fOut.bitrate, isHD));

        //2. reduce the config to the size of the video, stripping the high qualities
        if (isHD) {
            List<VideoOutput> filteredOutputsList = filterVideoOutputList(model.getHd_outputs(), fOut);
            model.setHd_outputs(filteredOutputsList);
            model.setSd_outputs(null);
        } else {
            List<VideoOutput> filteredOutputsList = filterVideoOutputList(model.getSd_outputs(), fOut);
            model.setHd_outputs(null);
            model.setSd_outputs(filteredOutputsList);
        }
        return model;
    }

    protected List<VideoOutput> filterVideoOutputList(List<VideoOutput> outputs, VideoInputMetadata metadata) {
        VideoOutput[] filteredOutputs = outputs
                .stream()
                .filter(o -> o.getWidth() <= metadata.width)
                .sorted((v1, v2) -> {
                    if (v1.getBitrate() < v2.getBitrate()) {
                        return 1;
                    }
                    if (v1.getBitrate() > v2.getBitrate()) {
                        return -1;
                    }

                    return 0;
                })
                .toArray(VideoOutput[]::new);
        if (filteredOutputs[0].getBitrate() > metadata.bitrate) {
            filteredOutputs[0].setBitrate((int) metadata.bitrate);
        }
        List<VideoOutput> filteredOutputsList = new ArrayList(Arrays.asList(filteredOutputs));

        return filteredOutputsList;
    }

    protected VideoInputMetadata extractWidthHeightBitrate(String ffmpegOutput) {
        Pattern p = Pattern.compile("^.*Stream.*Video.*\\s(?<width>\\d+)x(?<height>\\d+).*\\s(?<bitrate>\\d+)\\skb\\/s.*$", Pattern.MULTILINE);
        Matcher m = p.matcher(ffmpegOutput);
        if (m.find()) {
            return new VideoInputMetadata(Double.valueOf(m.group("width")), Double.valueOf(m.group("height")), Double.valueOf(m.group("bitrate")));
        }
        return null;
    }

    protected String extractFileName(String sourceURl) {
        return "demo";
    }

    protected class VideoInputMetadata {
        public double width;
        public double height;
        public double bitrate;

        public VideoInputMetadata(final double width, final double height, final double bitrate) {
            this.width = width;
            this.height = height;
            this.bitrate = bitrate;
        }
    }


}
