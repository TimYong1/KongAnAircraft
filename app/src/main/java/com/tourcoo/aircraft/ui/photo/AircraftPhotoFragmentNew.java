package com.tourcoo.aircraft.ui.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apkfuns.logutils.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.tourcoo.aircraft.widget.camera.CameraHelper;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.entity.media.MediaEntity;
import com.tourcoo.entity.media.MediaFileGroup;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.util.DateUtil;
import com.tourcoo.util.SizeUtil;
import com.tourcoo.util.ToastUtil;
import com.trello.rxlifecycle3.components.support.RxFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.media.FetchMediaTask;
import dji.sdk.media.FetchMediaTaskContent;
import dji.sdk.media.FetchMediaTaskScheduler;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING;
import static com.tourcoo.aircraft.ui.photo.LiveDataConstantNew.liveMediaDataList;
import static com.tourcoo.aircraft.ui.photo.PhotoPreviewActivityNew.EXTRA_CREATE_TIME;
import static com.tourcoo.aircraft.ui.photo.PhotoPreviewActivityNew.EXTRA_IMAGE_COUNT;
import static dji.common.camera.SettingsDefinitions.StorageLocation.UNKNOWN;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月10日13:52
 * @Email: 971613168@qq.com
 */
public class AircraftPhotoFragmentNew extends RxFragment {
    private boolean needRefresh = true;
    /**
     * 是否是顺序
     */
    private static boolean isDateOrder = true;
    public static final String TAG = "AircraftFragmentNew";
    private View contentView;
    private RecyclerView mCommonRecyclerView;
    private Handler mHandler;

    private MediaManager mediaManager;
    private FetchMediaTaskScheduler taskScheduler;
    private MediaManager.FileListState mFileListState;
    private List<FetchMediaTask> mediaTaskList = new ArrayList<>();
    private SettingsDefinitions.StorageLocation mediaType;
    private List<MediaFile> mediaFiles;
    private List<MediaEntity> mediaEntityList = new ArrayList<>();
    public static final int CODE_HIDE_PROGRESS_DIALOG = 201;
    private CommonCallbacks.CompletionCallback mCompletionCallback;
    private List<CommonCallbacks.CompletionCallback> completionCallbackList = new ArrayList<>();
    private List<CommonCallbacks.CompletionCallbackWith> completionCallbackWithList = new ArrayList<>();
    private GroupImageAdapter groupAdapter;
    private List<FetchMediaTask.Callback> mediaCallbackList = new ArrayList<>();
    public static final int REQUEST_CODE_PREVIEW = 1100;
    private final MediaManager.FileListStateListener mStateListener = new MediaManager.FileListStateListener() {
        @Override
        public void onFileListStateChange(MediaManager.FileListState fileListState) {
            mFileListState = fileListState;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.layout_recyclerview, container, false);
        mCommonRecyclerView = contentView.findViewById(R.id.mCommonRecyclerView);
        return contentView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void hideNavigation() {
        /**
         * 隐藏虚拟按键，并且全屏
         */
        Activity activity = getActivity();
        if (activity != null) {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(0);
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

    }


    private void initMediaManager() {
        Camera camera = CameraHelper.getInstance().getCamera();
        if (camera == null) {
            ToastUtil.showWarning("无人机未连接或相机模块未连接");
            return;
        }
        if (!camera.isMediaDownloadModeSupported()) {
            ToastUtil.showWarning("当前相机不支持媒体下载模式");
            return;
        }
        CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation> callbackWith = new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation>() {
            @Override
            public void onSuccess(SettingsDefinitions.StorageLocation storageLocation) {
                mediaType = storageLocation;
            }

            @Override
            public void onFailure(DJIError djiError) {
            }
        };
        mediaManager = camera.getMediaManager();
        if (mediaManager == null) {
            ToastUtil.showWarning("媒体管理器繁忙或发生错误请返回稍后重试");
            return;
        }
        completionCallbackWithList.add(callbackWith);
        camera.getStorageLocation(callbackWith);
        taskScheduler = mediaManager.getScheduler();
        mediaManager.addUpdateFileListStateListener(mStateListener);
        //进入媒体下载模式
        mCompletionCallback = new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtil.showWarningCondition("媒体模式设置错误：" + djiError.getDescription(), "媒体模式设置错误");
                    return;
                }
                boolean busy = (MediaManager.FileListState.SYNCING == mFileListState) || (MediaManager.FileListState.DELETING == mFileListState) || (mediaType == null) || (mediaType == UNKNOWN);
                if (busy) {
                    ToastUtil.showWarning("媒体管理器繁忙");
                    return;
                }
                ThreadManager.getDefault().execute(refreshListRunnable);
            }
        };
        completionCallbackList.add(mCompletionCallback);
        camera.enterPlayback(mCompletionCallback);
    }


    private final Runnable refreshListRunnable = new Runnable() {
        @Override
        public void run() {
            //开始获取媒体列表
            CommonCallbacks.CompletionCallback<DJIError> fileListCallback = new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtil.showWarningCondition("获取媒体文件列表错误：" + djiError.getDescription(), "获取媒体文件列表错误，请稍后重试");
                        mHandler.sendMessage(mHandler.obtainMessage(CODE_HIDE_PROGRESS_DIALOG, null));
                        return;
                    }
                    if (mediaType == SettingsDefinitions.StorageLocation.SDCARD) {
                        mediaFiles = mediaManager.getSDCardFileListSnapshot();
                    } else if (mediaType == SettingsDefinitions.StorageLocation.INTERNAL_STORAGE) {
                        mediaFiles = mediaManager.getInternalStorageFileListSnapshot();
                    }
                    if (mediaFiles == null) {
                        mediaFiles = new ArrayList<>();
                    }
                    listSortMedia(mediaFiles);
                    for (int i = mediaFiles.size() - 1; i >= 0; i--) {
                        final MediaFile media = mediaFiles.get(i);
                        MediaEntity mediaBean = new MediaEntity();
                        String fileName = media.getFileName();
                        int mediaType = media.getMediaType().value();
                        float durationInSeconds = media.getDurationInSeconds();
                        String dateCreated = media.getDateCreated();
                        mediaBean.setMedia(media);
                        mediaBean.setFileName(fileName);
                        mediaBean.setDateCreated(dateCreated);
                        mediaBean.setDurationInSeconds(durationInSeconds);
                        mediaBean.setMediaType(mediaType);
                        mediaEntityList.add(mediaBean);
                    }
                    List<MediaFileGroup> mediaGroupFileList = createMediaGroupFileList(groupMediaList(mediaEntityList));
                    liveMediaDataList.postValue(mediaFiles);
                    showMediaFileList(mediaGroupFileList);
                }
            };
            completionCallbackList.add(fileListCallback);
            mediaManager.refreshFileListOfStorageLocation(mediaType, fileListCallback);
        }
    };


    private void showMediaFileList(final List<MediaFileGroup> mediaFiles) {
        runUiThread(new Runnable() {
            @Override
            public void run() {
                if (groupAdapter == null) {
                    loadAdapter();
                } else {
                    groupAdapter.setNewData(mediaFiles);
                }
                if (taskScheduler != null) {
                    CommonCallbacks.CompletionCallback<DJIError> completionCallback = new CommonCallbacks.CompletionCallback<DJIError>() {
                        @Override
                        public void onResult(DJIError djiError) {
                            getThumbnails();
                        }
                    };
                    completionCallbackList.add(completionCallback);
                    taskScheduler.resume(completionCallback);
                }


            }
        });
    }

    private void runUiThread(Runnable runnable) {
        if (mHandler != null) {
            mHandler.post(runnable);
        }
    }

    private Map<String, List<MediaEntity>> groupMediaList(List<MediaEntity> list) {
        //初始化一个map
        Map<String, List<MediaEntity>> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (MediaEntity file : list) {
            if (file == null || file.getMedia() == null) {
                continue;
            }
            String key = DateUtil.parseDateString("yyyy-MM-dd", file.getMedia().getTimeCreated());
            List<MediaEntity> mediaList;
            if (map.containsKey(key)) {
                //map中存在以此id作为的key，将数据存放当前key的map中
                mediaList = map.get(key);
                if (mediaList != null) {
                    mediaList.add(file);
                }
                listSort(mediaList);
            } else {
                //map中不存在以此id作为的key，新建key用来存放数据
                List<MediaEntity> mediaFileList = new ArrayList<>();
                mediaFileList.add(file);
                map.put(key, mediaFileList);
                listSort(mediaFileList);
            }
        }
        //分组结束，map中的数据就是分组后的数据
        return map;
    }


    /**
     * 根据时间排序（其他排序如根据id排序也类似）
     *
     * @param list
     */
    private void listSort(List<MediaEntity> list) {
        //用Collections这个工具类传list进来排序
        Collections.sort(list, new Comparator<MediaEntity>() {
            @Override
            public int compare(MediaEntity o1, MediaEntity o2) {
                if (o1 != null && o1.getMedia() != null && o2 != null && o2.getMedia() != null) {
                    if (o1.getMedia().getTimeCreated() < o2.getMedia().getTimeCreated()) {
                        //小的放前面
                        return 1;
                    } else {
                        return -1;
                    }
                }
                return 0;
            }
        });
    }

    private void listSortMedia(List<MediaFile> list) {
        //用Collections这个工具类传list进来排序
        Collections.sort(list, new Comparator<MediaFile>() {
            @Override
            public int compare(MediaFile o1, MediaFile o2) {
                if (o1 != null && o2 != null) {
                    if (o1.getTimeCreated() < o2.getTimeCreated()) {
                        //小的放前面
                        return 1;
                    } else {
                        return -1;
                    }
                }
                return 0;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        hideNavigation();
        if (needRefresh) {
            loadAdapter();
            initMediaManager();
        }
    }

    private List<MediaFileGroup> createMediaGroupFileList(Map<String, List<MediaEntity>> listMap) {
        List<MediaFileGroup> mediaFileGroups = new ArrayList<>();
        String date;
        MediaFileGroup mediaFileGroup;
        List<MediaEntity> mediaFileList;
        Map<String, List<MediaEntity>> sortMap = sortMapByKey(listMap);
        for (Map.Entry<String, List<MediaEntity>> stringListEntry : sortMap.entrySet()) {
            date = stringListEntry.getKey();
            mediaFileGroup = new MediaFileGroup();
            mediaFileGroup.setTitle(date);
            mediaFileList = stringListEntry.getValue();
            mediaFileGroups.add(mediaFileGroup);
            int size = mediaFileList.size();
            for (int i = 0; i < size; i++) {
                mediaFileGroup = new MediaFileGroup();
                mediaFileGroup.setMediaEntity(mediaFileList.get(i));
                mediaFileGroup.setPosition(i);
                mediaFileGroups.add(mediaFileGroup);
            }
        }

        return mediaFileGroups;
    }


    /**
     * 使用 Map按key(日期)进行排序
     *
     * @param map
     * @return
     */
    public Map<String, List<MediaEntity>> sortMapByKey(Map<String, List<MediaEntity>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, List<MediaEntity>> sortMap = new TreeMap<>(
                new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }

    static class MapKeyComparator implements Comparator<String> {

        @Override
        public int compare(String str1, String str2) {
            Date date1 = DateUtil.stringParseToDate(str1);
            Date date2 = DateUtil.stringParseToDate(str2);
            return isDateOrder ? date2.compareTo(date1) : date1.compareTo(date2);
        }
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        releaseMediaManager();
        super.onDestroy();
    }


    private void releaseMediaManager() {
        if (mediaManager != null) {
            if (groupAdapter != null) {
                for (MediaFileGroup data : groupAdapter.getData()) {
                    if (data != null && data.getMediaEntity() != null && data.getMediaEntity().getMedia() != null) {
                        data.getMediaEntity().getMedia().resetThumbnail(null);
                        data.getMediaEntity().getMedia().stopFetchingFileData(null);
                    }
                }
                if (groupAdapter.getBitmapCacheMap() != null) {
                    for (Map.Entry<Long, Bitmap> longBitmapEntry : groupAdapter.getBitmapCacheMap().entrySet()) {
                        longBitmapEntry.getValue().recycle();
                    }
                }
                groupAdapter.getData().clear();
            }
            mediaManager.stop(null);
            mediaManager.removeFileListStateCallback(mStateListener);
            mediaManager.exitMediaDownloading();
            //如果媒体调度器存在任务，则移除所有任务
            if (taskScheduler != null) {
                taskScheduler.removeAllTasks();
                taskScheduler.suspend(null);
            }
            for (FetchMediaTask task : mediaTaskList) {
                task = null;
            }
            mediaTaskList.clear();
            mediaTaskList = null;
        }
        Camera camera = CameraHelper.getInstance().getCamera();
        if (camera != null) {
            CommonCallbacks.CompletionCallback<DJIError> exitPlayCallback = new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            };
            camera.exitPlayback(exitPlayCallback);
            CameraHelper.getInstance().setCameraModePhotoSingle();
            exitPlayCallback = null;
            mCompletionCallback = null;
        }
        unSetCallback();
    }

    private void unSetCallback() {
        Iterator<CommonCallbacks.CompletionCallback> iterator = completionCallbackList.iterator();
        while (iterator.hasNext()) {
            CommonCallbacks.CompletionCallback callback = iterator.next();
            callback = null;
            iterator.remove();
            LogUtils.w("执行了释放1");
        }
        Iterator<CommonCallbacks.CompletionCallbackWith> iterator1 = completionCallbackWithList.iterator();
        while (iterator1.hasNext()) {
            CommonCallbacks.CompletionCallbackWith callback1 = iterator1.next();
            callback1 = null;
            LogUtils.w("执行了释放2");
            iterator1.remove();
        }
        Iterator<MediaEntity> iterator2 = mediaEntityList.iterator();
        MediaEntity entity;
        while (iterator2.hasNext()) {
            entity = iterator2.next();
            if (entity.getMedia() != null) {
                entity.getMedia().stopFetchingFileData(null);
                entity.getMedia().resetPreview(null);
                entity.getMedia().resetThumbnail(null);
                entity.setMedia(null);
            }
            entity = null;
            iterator2.remove();
        }
        completionCallbackList = null;
        completionCallbackWithList = null;
        mediaEntityList = null;
        taskScheduler = null;
        mediaManager = null;
    }


    private void loadAdapter() {
        if (groupAdapter == null) {
            groupAdapter = new GroupImageAdapter(new ArrayList<>());
            int spanCount = 5;
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (groupAdapter.getItemViewType(position) == 0) {
                        return spanCount;
                    }
                    return 1;
                }
            });
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_empty_view, null);
            groupAdapter.setEmptyView(view);
            mCommonRecyclerView.addItemDecoration(new GridDividerItemDecoration(SizeUtil.dp2px(5f), ContextCompat.getColor(getContext(), R.color.black), false));
            groupAdapter.bindToRecyclerView(mCommonRecyclerView);
            mCommonRecyclerView.setLayoutManager(gridLayoutManager);
            mCommonRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    switch (newState) {
                        case SCROLL_STATE_IDLE:
                            groupAdapter.setScrolling(false);
                            needRefresh = true;
                            break;
                        case SCROLL_STATE_SETTLING:
                            groupAdapter.setScrolling(true);
                            needRefresh = false;
                            break;
                    }

                }


            });
            groupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    skipPhotoPreview(position);
                }
            });
        }
    }

    private void getThumbnails() {
        if (groupAdapter.getData().isEmpty()) {
            return;
        }
        List<MediaFileGroup> groupList = groupAdapter.getData();
        int size = groupList.size();
        MediaFileGroup mediaFileGroup;
        MediaEntity mediaEntity;
        MediaFile mediaFile;
        FetchMediaTask mediaTask;
        FetchMediaTask.Callback fetchMediaCallback;
        for (int i = 0; i < size; i++) {
            mediaFileGroup = groupList.get(i);
            if (mediaFileGroup == null || mediaFileGroup.getMediaEntity() == null || mediaFileGroup.getMediaEntity().getMedia() == null) {
                continue;
            }
            mediaEntity = mediaFileGroup.getMediaEntity();
            mediaFile = mediaEntity.getMedia();
            int index = groupAdapter.getBitmapCacheMap().indexOfKey(mediaFile.getTimeCreated());
            LogUtils.i(TAG + "当前索引为=" + index);
            if (index >= 0) {
                //说明当前有缓存bitmap 直接显示
                groupAdapter.notifyDataSetChanged();
                LogUtils.i(TAG + "当前有缓存bitmap 直接显示");
                continue;
            }
            fetchMediaCallback = new FetchMediaTask.Callback() {
                @Override
                public void onUpdate(MediaFile mediaFile, FetchMediaTaskContent fetchMediaTaskContent, DJIError djiError) {
                    if (null == djiError) {
                        //表示加载完成 已经获取到bitmap
                        runUiThread(new Runnable() {
                            @Override
                            public void run() {
                                groupAdapter.notifyDataSetChanged();
                            }
                        });

                        if (mediaFile != null && mediaFile.getThumbnail() != null) {
                            LogUtils.i(TAG + "添加缓存");
                            groupAdapter.getBitmapCacheMap().put(mediaFile.getTimeCreated(), mediaFile.getThumbnail());
                        }
                    }
                }
            };
            mediaTask = new FetchMediaTask(mediaFile, FetchMediaTaskContent.THUMBNAIL, fetchMediaCallback);
            mediaCallbackList.add(fetchMediaCallback);
            mediaTaskList.add(mediaTask);
            taskScheduler.moveTaskToEnd(mediaTask);
        }
    }


    private void skipPhotoPreview(int position) {
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            ToastUtil.showNormal("当前没有预览图");
            return;
        }
        if (position > groupAdapter.getData().size()) {
            ToastUtil.showNormal("当前状态无法预览或预览图不存在");
            return;
        }
        MediaFileGroup photoGroup = groupAdapter.getData().get(position);
        if (photoGroup == null || photoGroup.getMediaEntity() == null || photoGroup.getMediaEntity().getMedia() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(getActivity(), PhotoPreviewActivityNew.class);
        intent.putExtra(EXTRA_CREATE_TIME, photoGroup.getMediaEntity().getMedia().getTimeCreated());
        intent.putExtra(EXTRA_IMAGE_COUNT, mediaFiles.size());
        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PREVIEW:
                needRefresh = false;
                break;
            default:
                break;
        }
    }

}
