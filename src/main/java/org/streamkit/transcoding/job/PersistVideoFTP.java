package org.streamkit.transcoding.job;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.streamkit.transcoding.model.TranscodingModel;
import org.streamkit.transcoding.model.VideoDestination;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by @author Cosmin Stanciu on 15/07/15.
 */
@Component
public class PersistVideoFTP implements Step {
    public static final String DEFAULT_OUTPUT_FOLDER = "/tmp/streamkit/";
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
        logger.info("Persisting video to FTP");
        TranscodingModel model = (TranscodingModel) stepExecution.getJobExecution().getExecutionContext().get("model");

        if (! model.getDestination().getType().equals(VideoDestination.FTP)) {
            logger.info("Skipping step as the configuration didn't specify any FTP destination...");
            stepExecution.setStatus(BatchStatus.COMPLETED);
            return;
        }

        String destinationURL = model.getDestination().getUrl();
        ftpProps = getFtpProperties(destinationURL);
        this.ftpSessionFactory.setHost(ftpProps.getHost());
        this.ftpSessionFactory.setUsername(ftpProps.getUsername());
        this.ftpSessionFactory.setPassword(ftpProps.getPassword());
        this.ftpSessionFactory.setPort(ftpProps.getPort());

        String localFolder = (String) stepExecution.getJobExecution().getExecutionContext().get("output_local_folder");
        if ( localFolder == null ) {
            localFolder = DEFAULT_OUTPUT_FOLDER;
        }

        try {
            Files.walk(Paths.get(localFolder))
                    .filter((path) -> path.toFile().isFile() )
                    .forEachOrdered(this::sendToFtp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        stepExecution.setStatus(BatchStatus.COMPLETED);
    }

    private Boolean sendToFtp(Path file) {
        final File fileA = file.toFile();
        final Message<File> messageA = MessageBuilder.withPayload(fileA)
                .setHeader(FileHeaders.FILENAME, fileA.getName())
                .build();
        return this.toFtpChannel.send(messageA);
    }


    protected FTPServerProperties getFtpProperties (String destinationURL) {
        FTPServerProperties ftpProps = new FTPServerProperties();

        Pattern pattern = Pattern.compile("(ftp:\\/\\/|^)(?<username>\\w*):(?<password>[\\w-_!$#]*)@(?<host>[\\w\\.]*):?(?<port>\\d*)?(?<path>[\\/\\w]*)");
        Matcher matcher = pattern.matcher(destinationURL);

        while(matcher.find()) {
            ftpProps.setUsername(matcher.group("username"));
            ftpProps.setPassword(matcher.group("password"));
            String port = matcher.group("port");
            if (port != null && port.length() > 2) {
                ftpProps.setPort(Integer.parseInt(port));
            } else {
                ftpProps.setPort(21);
                port = "21";
            }
            //String url =  + ":" + port + matcher.group("path");
            ftpProps.setHost(matcher.group("host"));
        }

        return ftpProps;
    }


    @Bean
    public DefaultFtpSessionFactory ftpSessionFactory() {
        DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
        factory.setHost(ftpProps.getHost());
        factory.setPort(ftpProps.getPort());
        factory.setUsername(ftpProps.getUsername());
        factory.setPassword(ftpProps.getPassword());
        factory.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
        return factory;
    }

    @Bean
    @Qualifier("toFtpChannel")
    public PollableChannel remoteFileOutputChannel() {
        return new QueueChannel();
    }

    @Autowired
    @Qualifier(value="ftpOutboundFlow")
    private IntegrationFlow ftpOutboundFlow;

    @Bean
    public IntegrationFlow ftpOutboundFlow() {
        return IntegrationFlows.from("toFtpChannel")
                .handle(Ftp.outboundAdapter(this.ftpSessionFactory)
                                .useTemporaryFileName(false)
                                .fileNameExpression("headers['" + FileHeaders.FILENAME + "']")
                                .remoteDirectory("/")

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