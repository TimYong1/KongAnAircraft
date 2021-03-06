package com.tourcoo.entity.media;


import dji.sdk.media.MediaFile;

/**
 * @author :JenkinsZhou
 * @description :大疆媒体相关
 * @company :途酷科技
 * @date 2021年05月10日15:15
 * @Email: 971613168@qq.com
 */
public class MediaEntity {
    private int mediaType;
    private String fileName;
    private float durationInSeconds;
    private String dateCreated;
    private MediaFile media;
    private byte[] thumbnailBytes;
    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public float getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(float durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public MediaFile getMedia() {
        return media;
    }

    public void setMedia(MediaFile media) {
        this.media = media;
    }

    public byte[] getThumbnailBytes() {
        return thumbnailBytes;
    }

    public void setThumbnailBytes(byte[] thumbnailBytes) {
        this.thumbnailBytes = thumbnailBytes;
    }
}
