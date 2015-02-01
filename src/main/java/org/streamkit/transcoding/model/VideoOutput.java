package org.streamkit.transcoding.model;

/**
 * Created by ddascal on 22/01/15.
 */
public class VideoOutput {
    private int width;
    private int height;
    private int bitrate;
    private String audio_codec;
    private int audio_bitrate;
    private String video_codec;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getAudio_codec() {
        return audio_codec;
    }

    public void setAudio_codec(String audio_codec) {
        this.audio_codec = audio_codec;
    }

    public int getAudio_bitrate() {
        return audio_bitrate;
    }

    public void setAudio_bitrate(int audio_bitrate) {
        this.audio_bitrate = audio_bitrate;
    }

    public String getVideo_codec() {
        return video_codec;
    }

    public void setVideo_codec(String video_codec) {
        this.video_codec = video_codec;
    }

}
