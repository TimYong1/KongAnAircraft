package com.tourcoo.aircraft.widget.greendao;


import com.tourcoo.aircraft.ui.photo.PhotoLocalData;
import com.tourcoo.aircraft.ui.photo.PhotoLocalDataDao;
import com.tourcoo.aircraft.ui.sample.AircraftApplication;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import dji.sdk.media.MediaFile;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月31日14:30
 * @Email: 971613168@qq.com
 */
public class GreenDaoManager {

    private PhotoLocalDataDao photoLocalDataDao;

    public GreenDaoManager() {
        // 获取DAO实例
        photoLocalDataDao = AircraftApplication.getDaoSession().getPhotoLocalDataDao();
    }

    public PhotoLocalDataDao getDao() {
        return photoLocalDataDao;
    }


    // 根据条件查询数据
    public List<PhotoLocalData> findThumbnail(long timeCreated, String fileName) {
        QueryBuilder<PhotoLocalData> result = photoLocalDataDao.queryBuilder();
        //借助Property属性类提供的筛选方法
        result = result.where(PhotoLocalDataDao.Properties.FileName.eq(fileName), PhotoLocalDataDao.Properties.CreateTime.eq(timeCreated)).orderAsc(PhotoLocalDataDao.Properties.CreateTime);
        return result.list();
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public List<PhotoLocalData> findAllThumbnail() {
        QueryBuilder<PhotoLocalData> builder = photoLocalDataDao.queryBuilder();
        return builder.build().list();
    }

    public void  deleteAll() {
        photoLocalDataDao.deleteAll();
    }

    // 根据条件查询数据
    public void insertPhotoLocal(PhotoLocalData localData) {
        photoLocalDataDao.insert(localData);
    }
}
