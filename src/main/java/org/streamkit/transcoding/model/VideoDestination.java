package org.streamkit.transcoding.model;

/**
 * Created by ddascal on 22/01/15.
 */
public class VideoDestination {

    public final static String AWSS3 = "aws-s3";
    public final static String FTP = "ftp";
    public final static String LOCALFS = "local-fs";

    private String type = LOCALFS; // aws-s3, ftp, etc
    private String url;
    private String file_name_template;

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

    public String getFile_name_template() {
        return file_name_template;
    }

    public void setFile_name_template(String file_name_template) {
        this.file_name_template = file_name_template;
    }
}
