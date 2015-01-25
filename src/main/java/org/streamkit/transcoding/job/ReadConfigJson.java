package org.streamkit.transcoding.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.streamkit.transcoding.model.TranscodingModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by ddascal on 22/01/15.
 */
@Configuration
public class ReadConfigJson implements Step {
    private Logger logger = Logger.getLogger(ReadConfigJson.class.getName());
    private final static String DEFAULT_JSON_PATH = "/default_config.json";

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

        stepExecution.setStatus(BatchStatus.COMPLETED);
    }

    // Get default configuration and overwrite only the fields received from the custom configuration
    protected TranscodingModel getTranscodingModel (String customJson) throws JobInterruptedException {
        InputStream in = getClass().getResourceAsStream(DEFAULT_JSON_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        ObjectMapper mapper = new ObjectMapper();
        TranscodingModel model;
        try {
            model = mapper.readValue( reader, TranscodingModel.class);
            if (customJson != null) {
                mapper.readerForUpdating(model).readValue(customJson);
            }
        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        }
        return model;
    }

    protected String getConfigJsonProperty() {
        if (env != null) {
            return env.getProperty("configJson");
        }
        return "{}";
    }

}
