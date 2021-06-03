package com.tourcoo.util.cache;

import android.content.Context;
import android.os.Environment;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.ui.photo.PhotoLocalData;
import com.tourcoo.aircraft.widget.greendao.GreenDaoManager;
import com.tourcoo.threadpool.ThreadManager;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年06月02日17:13
 * @Email: 971613168@qq.com
 */
public class CacheDataManager {
    private String cacheSizeResult = "0.00MB";
    private final GreenDaoManager daoManager = new GreenDaoManager();
    /**
     * 考虑到数据库查找数据需要耗时 所以这里才用异步操作
     */
    private final ExecuteListener listener;

    /**
     * 获取整体缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    public String getTotalCacheSize(Context context) {
        ThreadManager.getIO().execute(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onExecuteStart();
                }
                long cacheSize;
                try {
                    cacheSize = getFolderSize(context.getCacheDir());
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        cacheSize += getFolderSize(context.getExternalCacheDir());
                    }
                    cacheSize += getImageCacheSize();
                    cacheSizeResult = getFormatSize(cacheSize);
                    if (listener != null) {
                        listener.onExecuteFinish(cacheSizeResult);
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                    e.printStackTrace();
                }
            }
        });
        return cacheSizeResult;

    }

    /**
     * 获取文件
     * Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
     * Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size
     */
    public static String getFormatSize(long size) {
        long kb = size / 1024;
        int m = (int) (kb / 1024);
        int kbs = (int) (kb % 1024);
        if (kbs == 0) {
            return m + "." + "00" + "MB";
        }
        if (kbs > 100&&kbs <1000) {
            return m + "." + kbs / 10 + "MB";
        }
        return m + "." + kbs + "MB";
    }

    /**
     * 清空方法
     *
     * @param context
     */
    public void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        daoManager.deleteAll();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public CacheDataManager(ExecuteListener listener) {
        this.listener = listener;
    }

    private long getImageCacheSize() {
        List<PhotoLocalData> cacheList = daoManager.findAllThumbnail();
        long size = 0;
        for (PhotoLocalData localData : cacheList) {
            if (localData != null && localData.getThumbnail() != null) {
                size += localData.getThumbnail().length;
            }
        }
        LogUtils.i("图片缓存大小=" + size);
        return size;
    }
}
