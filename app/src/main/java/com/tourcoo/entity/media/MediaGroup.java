package com.tourcoo.entity.media;

import java.util.List;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月10日16:55
 * @Email: 971613168@qq.com
 */
public class MediaGroup {
    private List<MediaEntity> mediaEntityList;
    private String dateTitle;

    public List<MediaEntity> getMediaEntityList() {
        return mediaEntityList;
    }

    public void setMediaEntityList(List<MediaEntity> mediaEntityList) {
        this.mediaEntityList = mediaEntityList;
    }

    public String getDateTitle() {
        return dateTitle;
    }

    public void setDateTitle(String dateTitle) {
        this.dateTitle = dateTitle;
    }
}
