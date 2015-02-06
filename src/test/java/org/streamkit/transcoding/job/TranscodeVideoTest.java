package org.streamkit.transcoding.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.streamkit.transcoding.model.TranscodingModel;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ReadConfigJson.class, TranscodeVideo.class})
public class TranscodeVideoTest {

    @Autowired
    ReadConfigJson config;

    @Autowired
    private TranscodeVideo transcodeVideo;

    private final static String sourceVideoFile = Thread.currentThread().getContextClassLoader().getResource("mira.flv").getPath();

    @Test
    public void testTranscode() throws Exception {
        TranscodingModel model = config.getTranscodingModel("{\"source\":\"" + sourceVideoFile + "\"}");
        TranscodeVideo.VideoInputMetadata inputMeta = transcodeVideo.getFFmpegMediaParameters(model);
        TranscodingModel reducedModel = transcodeVideo.reduceConfigToVideoMetadata(inputMeta, model);
        transcodeVideo.transcode(reducedModel);
        assertTrue(true);
    }

    @Test
    public void testExtractWidthHeightBitrate () {
        String ffmpegOutput = "Duration: 00:00:05.03, start: 0.000000, bitrate: 135 kb/s " +
                "Stream #0:0: Video: vp6f, yuv420p, 720x480, 1638 kb/s, 30 fps, 30 tbr, 1k tbn, 1k tbc \n" +
                "Stream #0:1: Audio: mp3, 44100 Hz, stereo, s16p, 262 kb/s";
        TranscodeVideo.VideoInputMetadata videoMetadata = transcodeVideo.extractWidthHeightBitrate(ffmpegOutput);
        assertEquals(480, videoMetadata.height, 0);
        assertEquals(1638, videoMetadata.video_bitrate, 0);
        assertEquals(262, videoMetadata.audio_bitrate, 0);
    }

    @Test
    public void testGetFFmpegMediaParameters () throws JobInterruptedException {
        TranscodingModel model = new TranscodingModel();
        model.setSource(sourceVideoFile);

        TranscodeVideo.VideoInputMetadata videoMetadata = transcodeVideo.getFFmpegMediaParameters(model);
        assertEquals(480, videoMetadata.height, 0);
        assertEquals(1638, videoMetadata.video_bitrate, 0);
        assertEquals(262, videoMetadata.audio_bitrate, 0);
    }

    @Test
    public void testReduceConfigToVideoMetadata_1 () throws JobInterruptedException {
        TranscodingModel model = config.getTranscodingModel("{\"source\":\"" + sourceVideoFile + "\"}");
        assertEquals(3, model.getOutputs().size());

        TranscodeVideo.VideoInputMetadata videoMetadata = new TranscodeVideo().new VideoInputMetadata();
        videoMetadata.setHeight(355);
        videoMetadata.setVideo_bitrate(100);
        videoMetadata.setAudio_bitrate(86);

        TranscodingModel reducedModel = transcodeVideo.reduceConfigToVideoMetadata(videoMetadata, model);
        assertEquals(1, reducedModel.getOutputs().size());

        assertEquals(355, reducedModel.getOutputs().get(0).getHeight());
        assertEquals(100, reducedModel.getOutputs().get(0).getVideo_bitrate());
        assertEquals(86, reducedModel.getOutputs().get(0).getAudio_bitrate());
    }

    @Test
    public void testReduceConfigToVideoMetadata_2 () throws JobInterruptedException {
        TranscodingModel model = config.getTranscodingModel("{\"source\":\"" + sourceVideoFile + "\"}");
        assertEquals(3, model.getOutputs().size());

        TranscodeVideo.VideoInputMetadata videoMetadata = new TranscodeVideo().new VideoInputMetadata();
        videoMetadata.setHeight(1980);
        videoMetadata.setVideo_bitrate(4000);
        videoMetadata.setAudio_bitrate(256);

        TranscodingModel reducedModel = transcodeVideo.reduceConfigToVideoMetadata(videoMetadata, model);
        assertEquals(3, reducedModel.getOutputs().size());

        assertEquals(1980, reducedModel.getOutputs().get(0).getHeight());
        assertEquals(2200, reducedModel.getOutputs().get(0).getVideo_bitrate());
        assertEquals(128, reducedModel.getOutputs().get(0).getAudio_bitrate());

        assertEquals(480, reducedModel.getOutputs().get(1).getHeight());
        assertEquals(600, reducedModel.getOutputs().get(1).getVideo_bitrate());
        assertEquals(96, reducedModel.getOutputs().get(1).getAudio_bitrate());

        assertEquals(144, reducedModel.getOutputs().get(2).getHeight());
        assertEquals(10, reducedModel.getOutputs().get(2).getVideo_bitrate());
        assertEquals(48, reducedModel.getOutputs().get(2).getAudio_bitrate());
    }

    @Test
    public void testReduceConfigToVideoMetadata_3 () throws JobInterruptedException {
        TranscodingModel model = config.getTranscodingModel("{\"source\":\"" + sourceVideoFile + "\"}");
        assertEquals(3, model.getOutputs().size());

        TranscodeVideo.VideoInputMetadata videoMetadata = new TranscodeVideo().new VideoInputMetadata();
        videoMetadata.setHeight(900);
        videoMetadata.setVideo_bitrate(4000);
        videoMetadata.setAudio_bitrate(256);

        TranscodingModel reducedModel = transcodeVideo.reduceConfigToVideoMetadata(videoMetadata, model);
        assertEquals(2, reducedModel.getOutputs().size());

        assertEquals(900, reducedModel.getOutputs().get(0).getHeight());
        assertEquals(2200, reducedModel.getOutputs().get(0).getVideo_bitrate());
        assertEquals(128, reducedModel.getOutputs().get(0).getAudio_bitrate());

        assertEquals(144, reducedModel.getOutputs().get(1).getHeight());
        assertEquals(10, reducedModel.getOutputs().get(1).getVideo_bitrate());
        assertEquals(48, reducedModel.getOutputs().get(1).getAudio_bitrate());
    }

    @Test
    public void testReduceConfigToVideoMetadata_4 () throws JobInterruptedException {
        TranscodingModel model = config.getTranscodingModel("{\"source\":\"" + sourceVideoFile + "\"}");
        assertEquals(3, model.getOutputs().size());

        TranscodeVideo.VideoInputMetadata videoMetadata = new TranscodeVideo().new VideoInputMetadata();
        videoMetadata.setHeight(300);
        videoMetadata.setVideo_bitrate(4000);
        videoMetadata.setAudio_bitrate(256);

        TranscodingModel reducedModel = transcodeVideo.reduceConfigToVideoMetadata(videoMetadata, model);
        assertEquals(1, reducedModel.getOutputs().size());

        assertEquals(300, reducedModel.getOutputs().get(0).getHeight());
        assertEquals(2200, reducedModel.getOutputs().get(0).getVideo_bitrate());
        assertEquals(128, reducedModel.getOutputs().get(0).getAudio_bitrate());

    }

    @Test
    public void testExtractFileName () {
        String filePath = "http://www.stremakit.net/content/video.file-name(1).avi";
        assertEquals("video.file-name(1)", transcodeVideo.extractFileName(filePath));
    }
}