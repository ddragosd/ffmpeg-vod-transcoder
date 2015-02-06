package org.streamkit.transcoding.model;

/**
 * Created by ddascal on 22/01/15.
 */
public class VideoOutput {
    private int height;
    private int video_bitrate;
    private String video_codec;
    private int audio_bitrate;
    private String audio_codec;


    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getVideo_bitrate() {
        return video_bitrate;
    }

    public void setVideo_bitrate(int video_bitrate) {
        this.video_bitrate = video_bitrate;
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
