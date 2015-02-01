package org.streamkit.transcoding.util;

/**
 * Created by @author Cosmin Stanciu on 01/02/15.
 */
public class FFmpegOutput {
    private double width;
    private double height;
    private double bitrate;

    public FFmpegOutput () {

    }

    public FFmpegOutput (double width, double height, double bitrate) {
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getBitrate() {
        return bitrate;
    }

    public void setBitrate(double bitrate) {
        this.bitrate = bitrate;
    }
}
