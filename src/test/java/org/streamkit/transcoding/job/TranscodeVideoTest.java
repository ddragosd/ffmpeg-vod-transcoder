package org.streamkit.transcoding.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.streamkit.transcoding.model.TranscodingModel;
import org.streamkit.transcoding.model.VideoOutput;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
                "Stream #0:0(eng): Video: h264 (Main) (avc1 / 0x31637661), yuv420p(tv), 640x480 [SAR 1:1 DAR 4:3], 28 kb/s, 30 fps, 30 tbr, 30k tbn, 60 tbc (default)";
        TranscodeVideo.VideoInputMetadata fOut = transcodeVideo.extractWidthHeightBitrate(ffmpegOutput);
        assertEquals(640, fOut.width, 0);
        assertEquals(480, fOut.height, 0);
        assertEquals(28d, fOut.bitrate, 0);
    }

    @Test
    public void testGetFFmpegMediaParameters () throws JobInterruptedException {
        TranscodingModel model = new TranscodingModel();
        model.setSource(sourceVideoFile);

        TranscodeVideo.VideoInputMetadata fOut = transcodeVideo.getFFmpegMediaParameters(model);
        assertEquals(640, fOut.width, 0);
        assertEquals(480, fOut.height, 0);
        assertEquals(28, fOut.bitrate, 0);
    }

    @Test
    public void testReduceConfigToVideoMetadata () {
        TranscodeVideo.VideoInputMetadata fOut = new TranscodeVideo().new VideoInputMetadata(640, 480, 100);

        TranscodingModel model = new TranscodingModel();
        model.setSource(sourceVideoFile);

        VideoOutput sdOut1 = new VideoOutput();
        sdOut1.setBitrate(64);
        sdOut1.setWidth(320);
        sdOut1.setHeight(200);
        VideoOutput sdOut2 = new VideoOutput();
        sdOut2.setBitrate(350);
        sdOut2.setWidth(640);
        sdOut2.setHeight(480);
        VideoOutput sdOut3 = new VideoOutput();
        sdOut3.setBitrate(800);
        sdOut3.setWidth(720);
        sdOut3.setHeight(575);

        List<VideoOutput> sdOutList = new ArrayList<>();
        sdOutList.add(sdOut1);
        sdOutList.add(sdOut2);
        sdOutList.add(sdOut3);

        model.setSd_outputs(sdOutList);
        assertEquals(3, model.getSd_outputs().size());

        TranscodingModel reducedModel = transcodeVideo.reduceConfigToVideoMetadata(fOut, model);
        assertEquals(2, reducedModel.getSd_outputs().size());
        assertEquals("The video bitrate should be at most the input bitrate", 100, reducedModel.getSd_outputs().get(0).getBitrate());
        assertNull(reducedModel.getHd_outputs());
    }
}