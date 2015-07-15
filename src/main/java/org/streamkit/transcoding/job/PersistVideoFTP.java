package org.streamkit.transcoding.job;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.streamkit.transcoding.model.TranscodingModel;

import java.io.File;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by @author Cosmin Stanciu on 15/07/15.
 */
@Component
public class PersistVideoFTP implements Step {
    private Logger logger = Logger.getLogger(PersistVideoFTP.class.getName());

    @Autowired
    private DefaultFtpSessionFactory ftpSessionFactory;

    @Autowired
    @Qualifier("toFtpChannel")
    private MessageChannel toFtpChannel;


    private FTPServerProperties ftpProps = new FTPServerProperties();


    @Override
    public String getName() {
        return "persist-video-FTP";
    }

    @Override
    public boolean isAllowStartIfComplete() {
        return true; // allow restart in case of failure
    }

    @Override
    public int getStartLimit() {
        return 5; // retry maximum 5 types to upload the file
    }

    @Override
    public void execute(StepExecution stepExecution) throws JobInterruptedException {
        TranscodingModel model = (TranscodingModel) stepExecution.getJobExecution().getExecutionContext().get("model");
        String destinationURL = model.getDestination().getUrl();
        ftpProps = getFtpProperties(destinationURL);

        String fileName = "foo.file";
        this.toFtpChannel.send(MessageBuilder.withPayload("foo")
                .setHeader(FileHeaders.FILENAME, fileName)
                .build());

        logger.info("Persisting video to FTP");
        stepExecution.setStatus(BatchStatus.COMPLETED);
    }


    protected FTPServerProperties getFtpProperties (String destinationURL) {
        FTPServerProperties ftpProps = new FTPServerProperties();

        Pattern pattern = Pattern.compile("(\\/\\/|^)(?<username>\\w*):(?<password>\\w*)@(?<host>[\\w\\.]*)(?<port>:\\d*)?(?<path>[\\/\\w]*)");
        Matcher matcher = pattern.matcher(destinationURL);

        while(matcher.find()) {
            ftpProps.setUsername(matcher.group("username"));
            ftpProps.setHost(matcher.group("password"));
            String port = matcher.group("port");
            if (port != null && port.startsWith(":") && port.length() > 2) {
                ftpProps.setPort(Integer.parseInt(port.substring(1)));
            } else {
                ftpProps.setPort(21);
                port = ":21";
            }
            String url = matcher.group("host") + port + matcher.group("path");
            ftpProps.setHost(url);
        }

        return ftpProps;
    }


    @Bean
    public DefaultFtpSessionFactory ftpSessionFactory() {
        DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
        factory.setHost(ftpProps.getHost());
        factory.setPort(ftpProps.port);
        factory.setUsername(ftpProps.username);
        factory.setPassword(ftpProps.password);
        return factory;
    }


    @Bean
    @Qualifier("toFtpChannel")
    public PollableChannel remoteFileOutputChannel() {
        return new QueueChannel();
    }

    @Bean
    public IntegrationFlow ftpOutboundFlow() {
        return IntegrationFlows.from("toFtpChannel")
                .handle(Ftp.outboundAdapter(this.ftpSessionFactory)
                                .useTemporaryFileName(false)
                                .fileNameExpression("headers['" + FileHeaders.FILENAME + "']")
                                .remoteDirectory("/tmp")
                ).get();
    }

    class FTPServerProperties {
        private String host = "localhost";
        private Integer port = 21;
        private String username = "username";
        private String password = "password";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}