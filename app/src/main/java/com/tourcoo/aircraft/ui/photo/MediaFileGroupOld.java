package com.tourcoo.aircraft.ui.photo;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;

import dji.sdk.media.MediaFile;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年04月15日14:23
 * @Email: 971613168@qq.com
 */
public class MediaFileGroupOld implements MultiItemEntity, Serializable {
    private String title;
    private MediaFile mediaFile;
    private int position;
    @Override
    public int getItemType() {
        return mediaFile != null ? 0 : 1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
