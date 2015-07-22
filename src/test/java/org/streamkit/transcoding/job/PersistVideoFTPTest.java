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
        PersistVideoFTP.FTPServerProperties props = ftp.getFtpProperties("ftp://usr1:pwdX@ftp.streamkit.net:34234/directory/path");
        assertEquals("usr1", props.getUsername());
        assertEquals("pwdX", props.getPassword());
        assertEquals(34234, props.getPort().intValue());
        assertEquals("ftp.streamkit.net", props.getHost());
    }

    @Test
    public void getFtpProperties_2_Test () throws JobInterruptedException {
        PersistVideoFTP.FTPServerProperties props = ftp.getFtpProperties("ftp://usr2:pwdY@ftp.streamkit.net/directory/path");
        assertEquals("usr2", props.getUsername());
        assertEquals("pwdY", props.getPassword());
        assertEquals(21, props.getPort().intValue());
        assertEquals("ftp.streamkit.net", props.getHost());
    }

    @Test
    public void getFtpProperties_3_Test () throws JobInterruptedException {
        PersistVideoFTP.FTPServerProperties props = ftp.getFtpProperties("ftp://username:pwd-!@ftp.streamkit.net");
        assertEquals("username", props.getUsername());
        assertEquals("pwd-!", props.getPassword());
        assertEquals(21, props.getPort().intValue());
        assertEquals("ftp.streamkit.net", props.getHost());
    }

    @Test
    public void getFtpProperties_with_port_no_subpath () throws JobInterruptedException {
        PersistVideoFTP.FTPServerProperties props = ftp.getFtpProperties("ftp://username:pwd_123_#$!@ftp.streamkit.net:1234");
        assertEquals("username", props.getUsername());
        assertEquals("pwd_123_#$!", props.getPassword());
        assertEquals(1234, props.getPort().intValue());
        assertEquals("ftp.streamkit.net", props.getHost());
    }


}
