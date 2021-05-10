package com.tourcoo.aircraft.ui.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.aircraft.widget.camera.CameraHelper;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.util.DateUtil;
import com.tourcoo.util.SizeUtil;
import com.tourcoo.util.ToastUtil;
import com.trello.rxlifecycle3.components.support.RxFragment;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import static com.tourcoo.aircraft.ui.photo.LiveDataConstant.liveData;
import static com.tourcoo.aircraft.ui.photo.PhotoPreviewActivity.EXTRA_CREATE_TIME;
import static com.tourcoo.aircraft.ui.photo.PhotoPreviewActivity.REQUEST_CODE_PREVIEW;

/**
 * @author :JenkinsZhou
 * @description : 无人机相册
 * @company :途酷科技
 * @date 2021年04月08日15:36
 * @Email: 971613168@qq.com
 */
public class AircraftPhotoFragment extends RxFragment {
    public static final String TAG = "AircraftPhotoFragment";
    private boolean mIsFirstShow;
    private Activity mContext;
    private View contentView;
    private RecyclerView mCommonRecyclerView;
    private PhotoAdapter photoAdapter;
    private Handler mHandler;
    private MediaManager.FileListStateListener stateListener;
    private MediaManager.FileListState mFileListState;
    private int spanCount = 5;
    private MediaManager mediaManager;
    private FetchMediaTaskScheduler taskScheduler;
    private FetchMediaTask.Callback mediaTaskCallback;
    private boolean needRefresh = true;
    private List<FetchMediaTask> mediaTaskList = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = (Activity) context;
        mHandler = new Handler(Looper.getMainLooper());
        mIsFirstShow = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.layout_recyclerview, container, false);
        mCommonRecyclerView = contentView.findViewById(R.id.mCommonRecyclerView);

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (needRefresh) {
            initMediaManager();
            loadAdapter();
        }
        hideNavigation();
    }


    private void loadAdapter() {
        if (photoAdapter == null) {
            photoAdapter = new PhotoAdapter(new ArrayList<>());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, spanCount);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (photoAdapter.getItemViewType(position) == 0) {
                        return 1;
                    }
                    return spanCount;
                }
            });
            photoAdapter.bindToRecyclerView(mCommonRecyclerView);
            mCommonRecyclerView.setLayoutManager(gridLayoutManager);
            mCommonRecyclerView.addItemDecoration(new GridDividerItemDecoration(SizeUtil.dp2px(5f), ContextCompat.getColor(mContext, R.color.black), false));
            mCommonRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    switch (newState) {
                        case SCROLL_STATE_IDLE:
                            photoAdapter.setScrolling(false);
                            break;
                        case SCROLL_STATE_SETTLING:
                            photoAdapter.setScrolling(true);
                            photoAdapter.setScrolling(true);
                            break;
                    }

                }


            });
            photoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    skipPhotoPreview(position);
                }
            });
        }
    }


    private void initMediaManager() {
        if (ProductManager.getProductInstance() == null) {
            return;
        }
        Camera camera = getCamera();
        if (camera == null) {
            if (photoAdapter != null && photoAdapter.getData() != null) {
                photoAdapter.getData().clear();
                photoAdapter.notifyDataSetChanged();
            }
            return;
        }
        if (!camera.isMediaDownloadModeSupported()) {
            return;
        }
        mediaManager = camera.getMediaManager();
        if (mediaManager == null) {
            return;
        }
        if (stateListener == null) {
            stateListener = new MediaManager.FileListStateListener() {
                @Override
                public void onFileListStateChange(MediaManager.FileListState fileListState) {
                    mFileListState = fileListState;
                }
            };
        }
        mediaManager.addUpdateFileListStateListener(stateListener);
        camera.enterPlayback(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    return;
                }
                if ((mFileListState == MediaManager.FileListState.SYNCING) || (mFileListState == MediaManager.FileListState.DELETING)) {
                    ToastUtil.showWarning("媒体设备正忙");
                    return;
                }
                mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.INTERNAL_STORAGE, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            LogUtils.i("执行了6：" + djiError);
                            return;
                        }
                        if (mFileListState == MediaManager.FileListState.INCOMPLETE) {
                            photoAdapter.getData().clear();
                        }
                        taskScheduler = mediaManager.getScheduler();
                        List<MediaFile> fileList = mediaManager.getInternalStorageFileListSnapshot();
                        listSort(fileList);
                        liveData.postValue(fileList);
                        Map<String, List<MediaFile>> groupResult = groupMediaList(fileList);
                        showMediaFileList(createMediaGroupFileList(groupResult));
                    }
                });

            }
        });
        if (mediaTaskCallback == null) {
            mediaTaskCallback = (mediaFile, fetchMediaTaskContent, djiError) -> {
                LogUtils.i("执行了7");
                if (null == djiError) {
                    runUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtils.i(TAG + (mediaFile.getThumbnail() != null));
                            LogUtils.i("执行了1");
                            photoAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    LogUtils.e(TAG + djiError);
                    LogUtils.i("执行了5");
                }
            };
        }
    }


    private void showMediaFileList(List<MediaFileGroup> mediaFiles) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                photoAdapter.setNewData(mediaFiles);
                if (taskScheduler != null) {
                    taskScheduler.resume(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            getThumbnails(mediaFiles);
                        }
                    });
                }
            }
        });
    }

    private void hideNavigation() {
        /**
         * 隐藏虚拟按键，并且全屏
         */
        View decorView = mContext.getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void unsetMediaManager() {
        Camera camera = getCamera();
        if (camera == null) {
            return;
        }
        if (mediaManager != null) {
            mediaManager.stop(null);
            if (stateListener != null) {
                mediaManager.removeFileListStateCallback(stateListener);
            }
            if (taskScheduler != null) {
                taskScheduler.removeAllTasks();
//                taskScheduler.destroy();
            }
            mediaManager.exitMediaDownloading();
            if (photoAdapter != null && photoAdapter.getData() != null) {
                List<MediaFileGroup> fileGroupList = photoAdapter.getData();
                MediaFileGroup fileGroup;
                MediaFile mediaFile;
                //正确 可删除多个
                Iterator<MediaFileGroup> iterator = fileGroupList.iterator();
                while (iterator.hasNext()) {
                    fileGroup = iterator.next();
                    if (fileGroup != null && fileGroup.getMediaFile() != null) {
                        mediaFile = fileGroup.getMediaFile();
                        mediaFile.stopFetchingFileData(null);
                        mediaFile = null;
                    }
                    iterator.remove();
                }
                photoAdapter.getData().clear();
            }
            if (mediaTaskList != null) {
                Iterator<FetchMediaTask> iterator = mediaTaskList.iterator();
                FetchMediaTask task;
                while (iterator.hasNext()) {
                    task = iterator.next();
                    task = null;
                    iterator.remove();
                }
            }
            taskScheduler = null;
            mediaTaskCallback=null;
            mediaManager = null;
            camera.exitPlayback(null);
            CameraHelper.getInstance().setCameraModePhotoSingle();
        }
    }

    private Camera getCamera() {
        if (!AircraftUtil.isCameraModuleAvailable()) {
            return null;
        }
        return ProductManager.getProductInstance().getCamera();
    }

    @Override
    public void onDestroy() {
        unsetMediaManager();
        super.onDestroy();
    }

    private void getThumbnails(List<MediaFileGroup> mediaFiles) {
        LogUtils.i("执行了8");
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            ToastUtil.showNormal("当前没有相册或无人机断开连接");
            return;
        }
        if (mediaTaskCallback == null) {
            ToastUtil.showWarningDebug("当前没有相册或无人机断开连接");
            return;
        }
        int size = mediaFiles.size();
        MediaFileGroup mediaFileGroup;
        for (int i = 0; i < size; i++) {
            mediaFileGroup = photoAdapter.getData().get(i);
            if (mediaFileGroup == null || mediaFileGroup.getMediaFile() == null) {
                continue;
            }
            FetchMediaTask fetchMediaTask = new FetchMediaTask(mediaFileGroup.getMediaFile(), FetchMediaTaskContent.THUMBNAIL, mediaTaskCallback);
            if (taskScheduler != null) {
                mediaTaskList.add(fetchMediaTask);
                taskScheduler.moveTaskToEnd(fetchMediaTask);
            } else {
                LogUtils.i("执行了2");
            }

        }
    }

    private void runUiThread(Runnable runnable) {
        if (mHandler != null) {
            mHandler.post(runnable);
        }
    }


    private Map<String, List<MediaFile>> groupMediaList(List<MediaFile> list) {
        //初始化一个map
        Map<String, List<MediaFile>> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (MediaFile file : list) {
            if (file == null) {
                continue;
            }
            String key = DateUtil.parseDateString("yyyy-MM-dd", file.getTimeCreated());
            List<MediaFile> mediaList;
            if (map.containsKey(key)) {
                //map中存在以此id作为的key，将数据存放当前key的map中
                mediaList = map.get(key);
                if (mediaList != null) {
                    mediaList.add(file);
                }
                listSort(mediaList);
            } else {
                //map中不存在以此id作为的key，新建key用来存放数据
                List<MediaFile> mediaFileList = new ArrayList<>();
                mediaFileList.add(file);
                map.put(key, mediaFileList);
                listSort(mediaFileList);
            }
        }
        //分组结束，map中的数据就是分组后的数据
        return map;
    }


    private List<MediaFileGroup> createMediaGroupFileList(Map<String, List<MediaFile>> listMap) {
        List<MediaFileGroup> mediaFileGroupList = new ArrayList<>();
        String date;
        MediaFileGroup mediaFileGroup;
        List<MediaFile> mediaFileList;
        for (Map.Entry<String, List<MediaFile>> stringListEntry : listMap.entrySet()) {
            date = stringListEntry.getKey();
            mediaFileGroup = new MediaFileGroup();
            mediaFileGroup.setTitle(date);
            mediaFileList = stringListEntry.getValue();
            mediaFileGroupList.add(mediaFileGroup);
            int size = mediaFileList.size();
            for (int i = 0; i < size; i++) {
                mediaFileGroup = new MediaFileGroup();
                mediaFileGroup.setMediaFile(mediaFileList.get(i));
                mediaFileGroup.setPosition(i);
                mediaFileGroupList.add(mediaFileGroup);
            }
        }
        return mediaFileGroupList;
    }

    private void skipPhotoPreview(int position) {
        if (position > photoAdapter.getData().size()) {
            ToastUtil.showNormal("当前状态无法预览或预览图不存在");
            return;
        }
        MediaFileGroup photoGroup = photoAdapter.getData().get(position);
        if (photoGroup == null || photoGroup.getMediaFile() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(mContext, PhotoPreviewActivity.class);
        intent.putExtra(EXTRA_CREATE_TIME, photoGroup.getMediaFile().getTimeCreated());
        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }

  /*  private void showImagePreview(MediaFile mediaFile){
        final  FetchMediaTask task = new FetchMediaTask(mediaFile, FetchMediaTaskContent.PREVIEW, new FetchMediaTask.Callback() {
            @Override
            public void onUpdate(MediaFile mediaFile, FetchMediaTaskContent fetchMediaTaskContent, DJIError djiError) {
                    if(djiError != null){
                        ToastUtil.showFailed("照片获取失败");
                        return;
                    }
                    if(mediaFile.getPreview() == null){
                        ToastUtil.showFailed("未获取到预览");
                        return;
                    }
                    final Bitmap previewBitMap = mediaFile.getPreview();
                    runUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
            }
        });
    }*/


    /**
     * 根据时间排序（其他排序如根据id排序也类似）
     *
     * @param list
     */
    private void listSort(List<MediaFile> list) {
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
