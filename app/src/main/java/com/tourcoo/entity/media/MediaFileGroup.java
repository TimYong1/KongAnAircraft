package com.tourcoo.entity.media;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import static com.tourcoo.constant.MediaConstant.MEDIA_TYPE_GROUP_CONTENT;
import static com.tourcoo.constant.MediaConstant.MEDIA_TYPE_GROUP_TITLE;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月11日17:05
 * @Email: 971613168@qq.com
 */
public class MediaFileGroup implements MultiItemEntity {
    private MediaEntity mediaEntity;
    private boolean isScrolling = false;
    private String title;
    private int position;
    @Override
    public int getItemType() {
        return (mediaEntity != null) && (mediaEntity.getMedia() != null) ? MEDIA_TYPE_GROUP_CONTENT  : MEDIA_TYPE_GROUP_TITLE;
    }

    public MediaEntity getMediaEntity() {
        return mediaEntity;
    }

    public void setMediaEntity(MediaEntity mediaEntity) {
        this.mediaEntity = mediaEntity;
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
