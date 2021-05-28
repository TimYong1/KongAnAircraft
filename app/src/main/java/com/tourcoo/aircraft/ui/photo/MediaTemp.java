package com.tourcoo.aircraft.ui.photo;

import android.graphics.Bitmap;
import android.util.SparseArray;

import androidx.collection.ArrayMap;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.media.MediaFile;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月13日14:16
 * @Email: 971613168@qq.com
 */
public class MediaTemp {
    static ArrayMap<Long, Bitmap> bitmapCacheMap = new ArrayMap<>();
    public static List<MediaFile> previewMediaFileList = new ArrayList<>();
}
