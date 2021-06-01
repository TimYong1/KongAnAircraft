package com.tourcoo.aircraft.ui.photo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月31日14:20
 * @Email: 971613168@qq.com
 */
@Entity
public class PhotoLocalData {
    private long createTime;
    private String fileName;
    private byte[] thumbnail;
    @Generated(hash = 2082889886)
    public PhotoLocalData(long createTime, String fileName, byte[] thumbnail) {
        this.createTime = createTime;
        this.fileName = fileName;
        this.thumbnail = thumbnail;
    }
    @Generated(hash = 337540697)
    public PhotoLocalData() {
    }
    public long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public String getFileName() {
        return this.fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public byte[] getThumbnail() {
        return this.thumbnail;
    }
    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

   
}
