package org.streamkit.transcoding.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the information about the file to be transcoded.
 */
public class TranscodingModel {
    /**
     * The file to be encoded: a file path or a URL
     */
    private String source;
    private VideoTrim trim;
    private List<VideoOutput> outputs = new ArrayList<>();
    private String snapshot_at;
    private String callback_url;
    private VideoDestination destination;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public VideoTrim getTrim() {
        return trim;
    }

    public void setTrim(VideoTrim trim) {
        this.trim = trim;
    }

    public List<VideoOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<VideoOutput> outputs) {
        this.outputs = outputs;
    }

    public String getSnapshot_at() {
        return snapshot_at;
    }

    public void setSnapshot_at(String snapshot_at) {
        this.snapshot_at = snapshot_at;
    }

    public String getCallback_url() {
        return callback_url;
    }

    public void setCallback_url(String callback_url) {
        this.callback_url = callback_url;
    }

    public VideoDestination getDestination() {
        return destination;
    }

    public void setDestination(VideoDestination destination) {
        this.destination = destination;
    }
}
