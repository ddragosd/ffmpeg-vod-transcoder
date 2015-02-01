package org.streamkit.transcoding.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ReadConfigJson.class, TranscodeVideo.class})
public class TranscodeVideoTest {

    @Autowired
    ReadConfigJson config;

    @Autowired
    private TranscodeVideo transcodeVideo;

    @Test
    public void testExecute() throws Exception {
        String sourceVideoFile = Thread.currentThread().getContextClassLoader().getResource("mira.mp4").getPath();
        transcodeVideo.transcode(config.getTranscodingModel("{\"source\":\"" + sourceVideoFile + "\"}"));
        assertTrue(true);
    }
}