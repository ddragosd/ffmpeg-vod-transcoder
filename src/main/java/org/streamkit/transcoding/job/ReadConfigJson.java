package org.streamkit.transcoding.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.streamkit.transcoding.model.TranscodingModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ddascal on 22/01/15.
 */
@Component
public class ReadConfigJson implements Step {
    private Logger logger = Logger.getLogger(ReadConfigJson.class.getName());
    private final static String DEFAULT_JSON_PATH = "/default_config.json";

    @Value("${configJson:#{null}}")
    private String configJson;

    @Autowired
    private Environment env;

    @Override
    public String getName() {
        return "read-config-json";
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
        logger.info("Extracting Configuration");

        String configJson = getConfigJsonProperty();

        TranscodingModel transcodingModel = getTranscodingModel(configJson);
        if ( transcodingModel == null ) {
            logger.severe("Could not read default configuration. Please verify that the file is in your classpath");
            stepExecution.setStatus(BatchStatus.FAILED);
        }
        stepExecution.getJobExecution().getExecutionContext().put("model", transcodingModel);
        stepExecution.setStatus(BatchStatus.COMPLETED);
    }

    // Get default configuration and overwrite only the fields received from the custom configuration
    protected TranscodingModel getTranscodingModel (String customJson) throws JobInterruptedException {
        InputStream in = getClass().getResourceAsStream(DEFAULT_JSON_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String decodedJson = customJson;

        // check to see if the input is Base64
        // Inspired from: http://stackoverflow.com/questions/8571501/how-to-check-whether-the-string-is-base64-encoded-or-not
        Pattern base64Pattern = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$", Pattern.MULTILINE);
        Matcher base64Matcher = base64Pattern.matcher(customJson);
        if ( base64Matcher.find()) {
            logger.info("Input JSON seems to be a Base64 encoded string. Decoding it  ...");
            //decode Base64
            byte[] jsonStringByte = Base64.getUrlDecoder().decode(customJson);
            decodedJson = new String(jsonStringByte);
        }

        ObjectMapper mapper = new ObjectMapper();
        TranscodingModel model;
        try {
            model = mapper.readValue( reader, TranscodingModel.class);
            if (decodedJson != null) {
                mapper.readerForUpdating(model).readValue(decodedJson);
            }
        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        }
        return model;
    }

    protected String getConfigJsonProperty() {
        if (this.configJson != null) {
            return this.configJson;
        }
        logger.warning("configJson not found ... ");
        return "{}";
    }

}
