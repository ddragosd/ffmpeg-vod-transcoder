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
@SpringApplicationConfiguration(classes = PersistVideoFTP.class)
public class PersistVideoFTPTest {

    @Autowired
    PersistVideoFTP ftp;

    @Test
    public void getFtpProperties_1_Test () throws JobInterruptedException {
        PersistVideoFTP.FTPServerProperties props = ftp.getFtpProperties("http://username:password@ftp.streamkit.net:34234/directory/path");
        assertEquals("username", props.getUsername());
        assertEquals("password", props.getPassword());
        assertEquals(34234, props.getPort().intValue());
        assertEquals("ftp.streamkit.net:34234/directory/path", props.getHost());
    }

    @Test
    public void getFtpProperties_2_Test () throws JobInterruptedException {
        PersistVideoFTP.FTPServerProperties props = ftp.getFtpProperties("http://username:password@ftp.streamkit.net/directory/path");
        assertEquals("username", props.getUsername());
        assertEquals("password", props.getPassword());
        assertEquals(21, props.getPort().intValue());
        assertEquals("ftp.streamkit.net:21/directory/path", props.getHost());
    }

    @Test
    public void getFtpProperties_3_Test () throws JobInterruptedException {
        PersistVideoFTP.FTPServerProperties props = ftp.getFtpProperties("http://username:password@ftp.streamkit.net");
        assertEquals("username", props.getUsername());
        assertEquals("password", props.getPassword());
        assertEquals(21, props.getPort().intValue());
        assertEquals("ftp.streamkit.net:21", props.getHost());
    }


}
