package org.streamkit.transcoding.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.streamkit.transcoding.model.TranscodingModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by ddascal on 22/01/15.
 */
@Configuration
public class ReadConfigJson implements Step {
    private Logger logger = Logger.getLogger(ReadConfigJson.class.getName());


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
        logger.info("Extracting Configuration : TODO");

        TranscodingModel transcodingModel = readDefaultTranscodingModel();
        if ( transcodingModel == null ) {
            logger.severe("Could not read default configuration. Please verify that the file is in your classpath");
            stepExecution.setStatus(BatchStatus.FAILED);
        }

        // TODO: merge with the config provided as args
//        String user_config = stepExecution.getExecutionContext().get("config-arg");
//        new ObjectMapper().readValue(user_config, Map.class)

        stepExecution.setStatus(BatchStatus.COMPLETED);
    }

    private TranscodingModel readDefaultTranscodingModel() throws JobInterruptedException {
        InputStream in = getClass().getResourceAsStream("/default_config.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        ObjectMapper jsonReader = new ObjectMapper();
        try {
            return (TranscodingModel) jsonReader.readValue( reader, TranscodingModel.class);
        } catch (IOException e) {
            throw new JobInterruptedException(e.getMessage());
        }
    }
}
