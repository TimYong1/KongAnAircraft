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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apkfuns.logutils.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.widget.camera.CameraHelper;
import com.tourcoo.aircraft.widget.greendao.GreenDaoManager;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.entity.media.MediaEntity;
import com.tourcoo.entity.media.MediaFileGroup;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.util.BitmapUtil;
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
import dji.ux.beta.core.extension.ViewExtensions;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING;
import static com.tourcoo.aircraft.ui.photo.LiveDataConstantNew.liveMediaDataList;
import static com.tourcoo.aircraft.ui.photo.MediaTemp.previewMediaFileList;
import static com.tourcoo.aircraft.ui.photo.PhotoPreviewActivityNew.EXTRA_CREATE_TIME;
import static com.tourcoo.aircraft.ui.photo.PhotoPreviewActivityNew.EXTRA_IMAGE_COUNT;
import static dji.common.camera.SettingsDefinitions.StorageLocation.UNKNOWN;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :????????????
 * @date 2021???05???10???13:52
 * @Email: 971613168@qq.com
 */
public class AircraftPhotoFragmentNew extends RxFragment {
    private boolean needRefresh = true;
    /**
     * ???????????????
     */
    private static boolean isDateOrder = true;
    private TextView tvPhotoCount;
    public static final String TAG = "AircraftFragmentNew";
    private View contentView;
    private RecyclerView mCommonRecyclerView;
    private Handler mHandler;
    private MediaEntity mediaEntity;
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
    private boolean isBackground = false;
    private GreenDaoManager daoManager;
    private ProgressBar pbLoading;
    private int photoCount;
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
        pbLoading = contentView.findViewById(R.id.pbLoading);
        return contentView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        daoManager = new GreenDaoManager();
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void hideNavigation() {
        /**
         * ?????????????????????????????????
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
            ToastUtil.showWarning("??????????????????????????????????????????");
            return;
        }
        if (!camera.isMediaDownloadModeSupported()) {
            ToastUtil.showWarning("???????????????????????????????????????");
            return;
        }
        if(AircraftUtil.isMavicAir()){
            doMediaDownloadMode(camera);
        }else {
            doEnterPlaybackMode(camera);
        }

    }


    private final Runnable refreshListRunnable = new Runnable() {
        @Override
        public void run() {
            //????????????????????????
            CommonCallbacks.CompletionCallback<DJIError> fileListCallback = new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtil.showWarningCondition("?????????????????????????????????" + djiError.getDescription(), "??????????????????????????????????????????????????????");
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
                    photoCount = mediaFiles.size();
                    findLocalImageAssignmentAndShow(mediaGroupFileList);
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
                closeLoading();
                if (groupAdapter == null) {
                    loadAdapter();
                } else {
                    groupAdapter.setNewData(mediaFiles);
                }
                if (taskScheduler != null) {
                    CommonCallbacks.CompletionCallback<DJIError> completionCallback = new CommonCallbacks.CompletionCallback<DJIError>() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (isBackground) {
                                LogUtils.e(TAG + "?????????????????? ???????????????");
                                return;
                            }
                            if (groupAdapter.getData().isEmpty()) {
                                return;
                            }
                            List<MediaFileGroup> groupList = new ArrayList<>(groupAdapter.getData());
                            loadMediaImageByList(groupList);
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
        //???????????????map
        Map<String, List<MediaEntity>> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (MediaEntity file : list) {
            if (file == null || file.getMedia() == null) {
                continue;
            }
            String key = DateUtil.parseDateString("yyyy-MM-dd", file.getMedia().getTimeCreated());
            if (key.contains("1979")) {
                continue;
            }
            List<MediaEntity> mediaList;
            if (map.containsKey(key)) {
                //map???????????????id?????????key????????????????????????key???map???
                mediaList = map.get(key);
                if (mediaList != null) {
                    mediaList.add(file);
                }
                listSort(mediaList);
            } else {
                //map??????????????????id?????????key?????????key??????????????????
                List<MediaEntity> mediaFileList = new ArrayList<>();
                mediaFileList.add(file);
                map.put(key, mediaFileList);
                listSort(mediaFileList);
            }
        }
        //???????????????map????????????????????????????????????
        return map;
    }


    /**
     * ??????????????????????????????????????????id??????????????????
     *
     * @param list
     */
    private void listSort(List<MediaEntity> list) {
        //???Collections??????????????????list????????????
        Collections.sort(list, new Comparator<MediaEntity>() {
            @Override
            public int compare(MediaEntity o1, MediaEntity o2) {
                if (o1 != null && o1.getMedia() != null && o2 != null && o2.getMedia() != null) {
                    if (o1.getMedia().getTimeCreated() < o2.getMedia().getTimeCreated()) {
                        //???????????????
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
        //???Collections??????????????????list????????????
        Collections.sort(list, new Comparator<MediaFile>() {
            @Override
            public int compare(MediaFile o1, MediaFile o2) {
                if (o1 != null && o2 != null) {
                    if (o1.getTimeCreated() < o2.getTimeCreated()) {
                        //???????????????
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
        isBackground = false;
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
     * ?????? Map???key(??????)????????????
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
                groupAdapter.getData().clear();
            }
            mediaManager.stop(null);
            mediaManager.removeFileListStateCallback(mStateListener);
            mediaManager.exitMediaDownloading();
            //?????????????????????????????????????????????????????????
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

        if (previewMediaFileList != null) {
            for (MediaFile mediaFile : previewMediaFileList) {
                if (mediaFile != null) {
                    mediaFile.resetPreview(null);
                    mediaFile.resetThumbnail(null);
                    mediaFile.stopFetchingFileData(null);
                }
            }
            if (mediaFiles != null) {
                mediaFiles.clear();
            }
        }

    }

    private void unSetCallback() {
        Iterator<CommonCallbacks.CompletionCallback> iterator = completionCallbackList.iterator();
        while (iterator.hasNext()) {
            CommonCallbacks.CompletionCallback callback = iterator.next();
            callback = null;
            iterator.remove();
            LogUtils.w("???????????????1");
        }
        Iterator<CommonCallbacks.CompletionCallbackWith> iterator1 = completionCallbackWithList.iterator();
        while (iterator1.hasNext()) {
            CommonCallbacks.CompletionCallbackWith callback1 = iterator1.next();
            callback1 = null;
            LogUtils.w("???????????????2");
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
            int spanCount = 6;
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (groupAdapter.getItemViewType(position) == BaseQuickAdapter.FOOTER_VIEW) {
                        return spanCount;
                    }
                    if (groupAdapter.getItemViewType(position) == 0) {
                        return spanCount;
                    }
                    return 1;
                }
            });
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_footer_view, null);
            tvPhotoCount = view.findViewById(R.id.tvPhotoCount);
            ViewExtensions.hide(tvPhotoCount);
            groupAdapter.addFooterView(view);
//            mCommonRecyclerView.addItemDecoration(new GridDividerItemDecoration(SizeUtil.dp2px(5f), ContextCompat.getColor(getContext(), R.color.black), false));
//            mCommonRecyclerView.addItemDecoration(new GridDividerItemDecoration(SizeUtil.dp2px(5f),SizeUtil.dp2px(114f), ContextCompat.getColor(getContext(), R.color.black)));
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


    private void skipPhotoPreview(int position) {
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            ToastUtil.showNormal("?????????????????????");
            return;
        }
        if (position > groupAdapter.getData().size()) {
            ToastUtil.showNormal("?????????????????????????????????????????????");
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

    @Override
    public void onPause() {
        super.onPause();
        isBackground = true;
    }

    private void loadMediaImageByList(List<MediaFileGroup> groupList) {
        if (groupList.isEmpty()) {
            ToastUtil.showSuccessDebug("????????????");
            showPhotoCount(photoCount);
            return;
        }
        MediaFileGroup mediaFileGroup = groupList.get(0);
        if (mediaFileGroup == null || mediaFileGroup.getMediaEntity() == null || mediaFileGroup.getMediaEntity().getMedia() == null) {
            groupList.remove(0);
            //????????????????????????
            loadMediaImageByList(groupList);
            return;
        }
        mediaEntity = mediaFileGroup.getMediaEntity();
        if (mediaEntity.getThumbnailBytes() != null && mediaEntity.getThumbnailBytes().length > 0) {
            //???????????????????????????????????????
            groupList.remove(0);
            //???????????????????????????
            loadMediaImageByList(groupList);
            return;
        }
        MediaFile mediaFile = mediaEntity.getMedia();
        //todo
            /*if (isBackground) {
                LogUtils.e(TAG + "?????????????????? ???????????????");
                return;
            }*/
        FetchMediaTask mediaTask;
        FetchMediaTask.Callback fetchMediaCallback;
        fetchMediaCallback = new FetchMediaTask.Callback() {
            @Override
            public void onUpdate(MediaFile mediaFile, FetchMediaTaskContent fetchMediaTaskContent, DJIError djiError) {
                handleMediaTaskCallback(groupList);
                if (null == djiError) {
                    //?????????????????? ???????????????bitmap
                    loadAndSaveImageCache(groupList);
                } else {
                    LogUtils.e(TAG + "fetchMediaCallback????????????" + djiError.getDescription());
                }
            }
        };
        mediaTask = new FetchMediaTask(mediaFile, FetchMediaTaskContent.THUMBNAIL, fetchMediaCallback);
        mediaCallbackList.add(fetchMediaCallback);
        mediaTaskList.add(mediaTask);
        taskScheduler.moveTaskToEnd(mediaTask);
    }


    private void loadAndSaveImageCache(List<MediaFileGroup> mediaFiles) {
        MediaFileGroup mediaFileGroup;
        MediaFile currentMedia;
        PhotoLocalData localData;
        PhotoLocalData sqData;
        MediaEntity mediaEntity;
        long createTime;
        String fileName;
        for (int i = 0; i < mediaFiles.size(); i++) {
            mediaFileGroup = mediaFiles.get(i);
            if (mediaFileGroup != null && mediaFileGroup.getMediaEntity() != null && mediaFileGroup.getMediaEntity().getMedia() != null) {
                mediaEntity = mediaFileGroup.getMediaEntity();
                currentMedia = mediaEntity.getMedia();
                if (currentMedia.getThumbnail() == null) {
                    continue;
                }
                createTime = currentMedia.getTimeCreated();
                fileName = currentMedia.getFileName();
                List<PhotoLocalData> localDataList = daoManager.findThumbnail(createTime, fileName);
                if (localDataList.isEmpty()) {
                    //??????????????????????????? ??? ????????????????????????
                    localData = new PhotoLocalData();
                    localData.setCreateTime(createTime);
                    localData.setFileName(fileName);
                    localData.setThumbnail(BitmapUtil.bitmap2Bytes(currentMedia.getThumbnail()));
                    daoManager.insertPhotoLocal(localData);
                    LogUtils.w(TAG + "?????????????????????" + createTime);
                } else {
                    //???????????????????????? ????????????
                    sqData = localDataList.get(0);
                    mediaEntity.setThumbnailBytes(sqData.getThumbnail());
                    LogUtils.i(TAG + "????????????????????????????????????" + sqData.getCreateTime());
                }
            }
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void findLocalImageAssignmentAndShow(List<MediaFileGroup> mediaGroupFileList) {
        showLoading();
        int size = mediaGroupFileList.size();
        MediaFileGroup mediaFileGroup;
        MediaEntity entity;
        MediaFile mediaFile;
        PhotoLocalData sqData;
        for (int i = 0; i < size; i++) {
            mediaFileGroup = mediaGroupFileList.get(i);
            if (mediaFileGroup != null && mediaFileGroup.getMediaEntity() != null && mediaFileGroup.getMediaEntity().getMedia() != null) {
                entity = mediaFileGroup.getMediaEntity();
                mediaFile = entity.getMedia();
                List<PhotoLocalData> localDataList = daoManager.findThumbnail(mediaFile.getTimeCreated(), mediaFile.getFileName());
                if (!localDataList.isEmpty()) {
                    //???????????????????????? ????????????
                    sqData = localDataList.get(0);
                    if (sqData != null) {
                        entity.setThumbnailBytes(sqData.getThumbnail());
                        LogUtils.i(TAG + "????????????????????????????????????????????????mediaFile");
                    }
                }
            }
        }
        showMediaFileList(mediaGroupFileList);
    }

    private void showLoading() {
        runUiThread(new Runnable() {
            @Override
            public void run() {
                ViewExtensions.show(pbLoading);
            }
        });
    }

    private void closeLoading() {
        runUiThread(new Runnable() {
            @Override
            public void run() {
                ViewExtensions.hide(pbLoading);
            }
        });
    }


    private void handleMediaTaskCallback(List<MediaFileGroup> groupList) {
        runUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(TAG + "?????????notifyDataSetChanged");
                groupAdapter.notifyDataSetChanged();
                groupList.remove(0);
                loadMediaImageByList(groupList);
            }
        });
    }

    private void showPhotoCount(int photoCount) {
        if (tvPhotoCount != null) {
            tvPhotoCount.setText("???" + photoCount + "?????????");
            ViewExtensions.show(tvPhotoCount);
        }
    }

    private void doEnterPlaybackMode(Camera camera){
        CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation> callbackWith = new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation>() {
            @Override
            public void onSuccess(SettingsDefinitions.StorageLocation storageLocation) {
                mediaType = storageLocation;
            }

            @Override
            public void onFailure(DJIError djiError) {
                ToastUtil.showWarning("???????????????????????????????????????");
            }
        };
        mediaManager = camera.getMediaManager();
        if (mediaManager == null) {
            ToastUtil.showWarning("???????????????????????????????????????");
            closeLoading();
            return;
        }
        completionCallbackWithList.add(callbackWith);
        camera.getStorageLocation(callbackWith);
        taskScheduler = mediaManager.getScheduler();
        mediaManager.addUpdateFileListStateListener(mStateListener);
        //????????????????????????
        mCompletionCallback = new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtil.showWarningCondition("???????????????????????????" + djiError.getDescription(), "???????????????????????????????????????");
                    return;
                }
                boolean busy = (MediaManager.FileListState.SYNCING == mFileListState) || (MediaManager.FileListState.DELETING == mFileListState) || (mediaType == null) || (mediaType == UNKNOWN);
                if (busy) {
                    ToastUtil.showWarning("?????????????????????");
                    return;
                }
                ThreadManager.getDefault().execute(refreshListRunnable);
            }
        };
        completionCallbackList.add(mCompletionCallback);
        camera.enterPlayback(mCompletionCallback);
    }


    private void doMediaDownloadMode(Camera camera){
        CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation> callbackWith = new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation>() {
            @Override
            public void onSuccess(SettingsDefinitions.StorageLocation storageLocation) {
                mediaType = storageLocation;
            }

            @Override
            public void onFailure(DJIError djiError) {
                ToastUtil.showWarning("???????????????????????????????????????");
            }
        };
        mediaManager = camera.getMediaManager();
        if (mediaManager == null) {
            ToastUtil.showWarning("???????????????????????????????????????");
            closeLoading();
            return;
        }
        completionCallbackWithList.add(callbackWith);
        camera.getStorageLocation(callbackWith);
        taskScheduler = mediaManager.getScheduler();
        mediaManager.addUpdateFileListStateListener(mStateListener);
       camera.setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, new CommonCallbacks.CompletionCallback() {
           @Override
           public void onResult(DJIError djiError) {
               if (djiError != null) {
                   ToastUtil.showWarningCondition("???????????????????????????" + djiError.getDescription(), "???????????????????????????????????????");
                   return;
               }
               boolean busy = (MediaManager.FileListState.SYNCING == mFileListState) || (MediaManager.FileListState.DELETING == mFileListState) || (mediaType == null) || (mediaType == UNKNOWN);
               if (busy) {
                   ToastUtil.showWarning("?????????????????????");
                   return;
               }
               ThreadManager.getDefault().execute(refreshListRunnable);
           }
       });

    }
}
