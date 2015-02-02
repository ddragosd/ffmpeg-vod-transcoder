package org.streamkit.transcoding.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.streamkit.transcoding.model.TranscodingModel;

import static org.junit.Assert.assertEquals;

/**
 * Created by @author Cosmin Stanciu on 24/01/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ReadConfigJson.class)
public class ReadConfigJsonTest {

    @Autowired
    ReadConfigJson config;

    @Test
    public void readDefaultTranscodingModelTest () throws JobInterruptedException {
        String customConfigJson = "{\"callback_url\": \"custom_value\"}";
        TranscodingModel transcodingModel = config.getTranscodingModel(customConfigJson);

        assertEquals("00:00:00", transcodingModel.getSnapshot_at());
        assertEquals("custom_value", transcodingModel.getCallback_url());
        assertEquals(0, transcodingModel.getTrim().getStart());
        assertEquals(0, transcodingModel.getTrim().getEnd());
        assertEquals(3, transcodingModel.getSd_outputs().size());
        assertEquals(3, transcodingModel.getHd_outputs().size());
    }

    /*@Test
    public void getConfigJsonPropertyTest() {
        assertEquals(null, config.getConfigJsonProperty());
    }*/

}
