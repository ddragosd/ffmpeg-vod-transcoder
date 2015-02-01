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
    private List<VideoOutput> sd_outputs = new ArrayList<>();
    private List<VideoOutput> hd_outputs = new ArrayList<>();
    private String snapshot_at;

    private String callback_url;
    private VideoDestination destination;

    private VideoTrim trim;

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

    public List<VideoOutput> getSd_outputs() {
        return sd_outputs;
    }

    public void setSd_outputs(List<VideoOutput> sd_outputs) {
        this.sd_outputs = sd_outputs;
    }

    public List<VideoOutput> getHd_outputs() {
        return hd_outputs;
    }

    public void setHd_outputs(List<VideoOutput> hd_outputs) {
        this.hd_outputs = hd_outputs;
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
