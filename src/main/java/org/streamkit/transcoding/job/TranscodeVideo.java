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

    /**
     * The location where the encoded files are written.
     */
    private static final String OUTPUT_LOCATION = "/tmp/streamkit/";

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

        List<VideoOutput> outputs = reducedModel.getOutputs() != null ? reducedModel.getOutputs() : reducedModel.getOutputs();

        String fileName = this.extractFileName(reducedModel.getSource());
        StringBuilder ffmpegCmdBuilder = new StringBuilder();
        ffmpegCmdBuilder.append("ffmpeg -i ")
                .append(reducedModel.getSource())
                .append(" ")
                .append(outputs.stream()
                                .map(o -> String.format("-c:a %s -b:a %dk -c:v %s -vf scale=%d:%d -x264opts bitrate=%d %s ",
                                                o.getAudio_codec(), o.getAudio_bitrate(), o.getVideo_codec(), -2, o.getHeight(), o.getVideo_bitrate(),
                                                OUTPUT_LOCATION + reducedModel.getDestination().getFile_name_template()
                                                        .replaceAll("\\$width", "-2")
                                                        .replaceAll("\\$height", String.valueOf(o.getHeight()))
                                                        .replaceAll("\\$bitrate", String.valueOf(o.getVideo_bitrate()))
                                                        .replaceAll("\\$originalFileName", fileName) + ".mp4")
                                ).reduce("", (a, b) -> a + b)
                );
        String[] commands = ffmpegCmdBuilder.toString().split(" ");
        // TODO: include trim
        logger.info(String.format("Running FFMPEG command: \n %s \n", ffmpegCmdBuilder.toString()));

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
            p.waitFor();
            int processExitValue = p.exitValue();
            logger.info("FFMmpeg finished with exitValue=" + processExitValue);

//            assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
////            assert pb.redirectOutput().file() == log;
//            assert pb.redirectOutput() == ProcessBuilder.Redirect.PIPE;
//            assert p.getInputStream().read() == -1;
        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            VideoInputMetadata videoMetadata = extractWidthHeightBitrate(ffmpegOutput);
            logger.info(String.format("Video Info:\n%s\n\n", ffmpegOutput));
            if (videoMetadata == null) {
                throw new JobInterruptedException("Could not read video info.");
            }

            return videoMetadata;

        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        }
    }


    protected TranscodingModel reduceConfigToVideoMetadata(VideoInputMetadata videoMetadata, TranscodingModel model) {

        //determine video parameters
        logger.info(String.format("Height=%.2f, Bitrate=%.2f", videoMetadata.height, videoMetadata.video_bitrate));

        //reduce the config to the size of the video, stripping the high qualities
        List<VideoOutput> filteredOutputsList = filterVideoOutputList(model.getOutputs(), videoMetadata);
        model.setOutputs(filteredOutputsList);

        return model;
    }

    protected List<VideoOutput> filterVideoOutputList(List<VideoOutput> outputs, VideoInputMetadata metadata) {
        // List for the config list ordered from highest to lowest
        VideoOutput[] orderedOutput = outputs
                .stream()
                .sorted((v1, v2) -> {
                    if (v1.getVideo_bitrate() < v2.getVideo_bitrate()) {
                        return 1;
                    }
                    if (v1.getVideo_bitrate() > v2.getVideo_bitrate()) {
                        return -1;
                    }

                    return 0;
                })
                .toArray(VideoOutput[]::new);

        // List for the filtered by resolution and bitrate from highest to lowest
        VideoOutput[] filteredOutputs = outputs
                .stream()
                .filter(o -> o.getHeight() <= metadata.height && o.getVideo_bitrate() <= metadata.video_bitrate)
                .sorted((v1, v2) -> {
                    if (v1.getVideo_bitrate() < v2.getVideo_bitrate()) {
                        return 1;
                    }
                    if (v1.getVideo_bitrate() > v2.getVideo_bitrate()) {
                        return -1;
                    }

                    return 0;
                })
                .toArray(VideoOutput[]::new);

        // Highest video_bitrate should have default input value or the max value from configuration
        if (metadata.video_bitrate < orderedOutput[0].getVideo_bitrate()) {
            filteredOutputs[0].setVideo_bitrate((int) metadata.video_bitrate);
        } else {
            filteredOutputs[0].setVideo_bitrate(orderedOutput[0].getVideo_bitrate());
        }

        // Highest audio_bitrate should have default input value or the max value from configuration
        if (metadata.audio_bitrate < orderedOutput[0].getAudio_bitrate()) {
            filteredOutputs[0].setAudio_bitrate((int) metadata.audio_bitrate);
        } else {
            filteredOutputs[0].setAudio_bitrate(orderedOutput[0].getAudio_bitrate());
        }

        // Resolution should always have the input value for the highest quality
        if (filteredOutputs[0].getHeight() < metadata.height) {
            filteredOutputs[0].setHeight((int) metadata.height);
        }

        List<VideoOutput> filteredOutputsList = new ArrayList(Arrays.asList(filteredOutputs));

        return filteredOutputsList;
    }

    protected VideoInputMetadata extractWidthHeightBitrate(String ffmpegOutput) {
        Pattern pVideo = Pattern.compile("^.*Stream.*Video.*\\s(?<width>\\d+)x(?<height>\\d+).*\\s(?<videobitrate>\\d+)\\skb\\/s.*$", Pattern.MULTILINE);
        Pattern pAudio = Pattern.compile("^.*Stream.*Audio.*\\s(?<audiobitrate>\\d+)\\skb\\/s.*$", Pattern.MULTILINE);

        VideoInputMetadata metadata = new VideoInputMetadata();
        Matcher mVideo = pVideo.matcher(ffmpegOutput);
        if (mVideo.find()) {
            metadata.setHeight(Double.valueOf(mVideo.group("height")));
            metadata.setVideo_bitrate(Double.valueOf(mVideo.group("videobitrate")));
        }
        Matcher mAudio = pAudio.matcher(ffmpegOutput);
        if (mAudio.find()) {
            metadata.setAudio_bitrate(Double.valueOf(mAudio.group("audiobitrate")));
        }
        return metadata;
    }

    protected String extractFileName(String sourceURl) {
        Pattern p = Pattern.compile("\\/(?<filename>[^\\/]*)\\.(?<extension>\\w*)$");
        Matcher m = p.matcher(sourceURl);
        if (m.find()) {
            return m.group("filename");
        }
        return null;
    }

    protected class VideoInputMetadata {
        public double height;
        public double video_bitrate;
        public double audio_bitrate;

        public VideoInputMetadata() {
        }

        public VideoInputMetadata(double height, double video_bitrate, double audio_bitrate) {
            this.height = height;
            this.video_bitrate = video_bitrate;
            this.audio_bitrate = audio_bitrate;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public void setVideo_bitrate(double video_bitrate) {
            this.video_bitrate = video_bitrate;
        }

        public void setAudio_bitrate(double audio_bitrate) {
            this.audio_bitrate = audio_bitrate;
        }
    }


}
