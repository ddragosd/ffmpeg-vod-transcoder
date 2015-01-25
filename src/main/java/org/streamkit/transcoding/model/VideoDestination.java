package org.streamkit.transcoding.model;

/**
 * Created by ddascal on 22/01/15.
 */
public class VideoDestination {

    private String type; // aws-s3, ftp, etc
    private String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
