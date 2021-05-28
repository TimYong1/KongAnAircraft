package com.tourcoo.aircraft.ui.photo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import com.apkfuns.logutils.LogUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.aircraft.widget.camera.CameraHelper;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.util.DateUtil;
import com.tourcoo.util.GlideManager;
import com.tourcoo.util.StringUtil;
import com.tourcoo.util.ToastUtil;
import com.tourcoo.view.ViewPagerFixed;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.media.FetchMediaTask;
import dji.sdk.media.FetchMediaTaskContent;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

import static com.tourcoo.aircraft.ui.photo.MediaTemp.bitmapCacheMap;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月12日15:13
 * @Email: 971613168@qq.com
 */
public class PhotoPreviewActivityNew extends RxAppCompatActivity implements View.OnClickListener {
    private ViewPagerFixed viewPagerFixed;
    private TextView tvPhotoTime;
    private Long mediaCreateTime;
    private MediaManager mediaManager;
    public static final String TAG = "PhotoPreviewActivityNew";
    private PhotoPreViewAdapter preViewAdapter;
    private ArrayList<View> mViewList = new ArrayList<>();
    private List<MediaFile> mediaFileList;
    private ArrayList<PhotoView> photoViewArrayList = new ArrayList<>();
    public static final String EXTRA_CREATE_TIME = "EXTRA_CREATE_TIME";
    public static final String EXTRA_IMAGE_COUNT = "EXTRA_IMAGE_COUNT";
    private List<FetchMediaTask> mediaTaskList = new ArrayList<>();
    private boolean isFront = true;
    private ProgressBar pbLoading;
    private String currentTime ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);
        initView();
        loadImage();
        initMediaManager();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigation();
        isFront = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFront = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                finish();
                break;
            default:
                break;
        }
    }

    private void initView() {
        findViewById(R.id.ivBack).setOnClickListener(this);
        viewPagerFixed = findViewById(R.id.vpPhoto);
        tvPhotoTime = findViewById(R.id.tvPhotoTime);
        pbLoading = findViewById(R.id.pbLoading);
        viewPagerFixed.setOffscreenPageLimit(3);
        getIntent().getLongExtra(EXTRA_CREATE_TIME, -1);
    }


    private void hideNavigation() {
        /**
         * 隐藏虚拟按键，并且全屏
         */
        View decorView = this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void initAdapter() {
        if (null == preViewAdapter) {
            if (mediaFileList == null) {
                mediaFileList = new ArrayList<>(mViewList.size());
            }
            viewPagerFixed.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    setViewGone(pbLoading,true);
                    if (position >= mediaFileList.size() || position >= mViewList.size()) {
                        ToastUtil.showWarningDebug("拦截了："+position+mediaFileList.size());
                        return;
                    }
                    showImagePreview(mediaFileList.get(position), mViewList.get(position));
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    switch (state) {
                        case 1:
                            tvPhotoTime.setText(getString(R.string.loading));
                            break;
                        case 2:
                            setViewGone(pbLoading,true);
                            tvPhotoTime.setText(getString(R.string.loading));
                            break;
                        default:
                            break;
                    }
//                    arg0 ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做。

                }
            });
            preViewAdapter = new PhotoPreViewAdapter(mViewList, mediaFileList);
            viewPagerFixed.setAdapter(preViewAdapter);

        }
    }

    private void loadImage() {
        LiveDataConstantNew.liveMediaDataList.observe(this, new Observer<List<MediaFile>>() {
            @Override
            public void onChanged(List<MediaFile> mediaFiles) {
                if (!isFront) {
                    LogUtils.w(TAG + "当前不在前台运行 需要拦截");
                    return;
                }
                if (mediaFiles == null) {
                    return;
                }
                LogUtils.i(TAG + "执行了1=" + mediaFiles.size());
                mediaFileList.clear();
                mediaFileList.addAll(mediaFiles);
                int position = findMediaPosition(mediaFileList);
                if (position < 0) {
                    ToastUtil.showFailedDebug("未获取到预览");
                    return;
                }
                viewPagerFixed.setCurrentItem(position);
                LogUtils.i(TAG + "执行了2=" + mediaFileList.size());
                try {
                    showImagePreview(mediaFileList.get(position), mViewList.get(position));
                } catch (IndexOutOfBoundsException e) {
                    LogUtils.e(TAG + e.toString());
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    private void release() {
        releaseMediaManager();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            ToastUtil.showWarning("未获取到图片信息");
            return;
        }
        int size = intent.getIntExtra(EXTRA_IMAGE_COUNT, -1);
        mediaCreateTime = getIntent().getLongExtra(EXTRA_CREATE_TIME, -1);
        View parentView;
        PhotoView imageView;
        for (int i = 0; i < size; i++) {
            parentView = LayoutInflater.from(this).inflate(R.layout.item_photo_preview, null);
            mViewList.add(parentView);
            imageView = parentView.findViewById(R.id.photoPreview);
            photoViewArrayList.add(imageView);
        }
        initAdapter();
    }


    private int findMediaPosition(List<MediaFile> list) {
        if (list == null || list.isEmpty() || mediaCreateTime == null || mediaCreateTime <= 0) {
            LogUtils.e(TAG + "mediaCreateTime=" + mediaCreateTime);
            return -1;
        }
        int size = list.size();
        MediaFile mediaFile;
        for (int i = 0; i < size; i++) {
            mediaFile = list.get(i);
            if (mediaFile.getTimeCreated() == mediaCreateTime) {
                LogUtils.i("找到了媒体文件对应位置=" + i);
                return i;
            }
        }
        return -1;
    }

    private void showImagePreview(MediaFile mediaFile, View parentView) {
        if (mediaFile == null) {
            ToastUtil.showWarning("当前预览图为空");
            return;
        }
        FetchMediaTask task = new FetchMediaTask(mediaFile, FetchMediaTaskContent.PREVIEW, new FetchMediaTask.Callback() {
            @Override
            public void onUpdate(MediaFile mediaFile, FetchMediaTaskContent fetchMediaTaskContent, DJIError djiError) {
                setViewGone(pbLoading,false);
                if (djiError != null) {
                    ToastUtil.showFailedDebug("照片获取失败" + djiError.getDescription(), "照片获取失败");
                    return;
                }
                if (MediaTemp.previewMediaFileList == null) {
                    MediaTemp.previewMediaFileList = new ArrayList<>();
                }
                if (mediaFile.getPreview() == null) {
                    ToastUtil.showFailed("未获取到预览照片");
                    return;
                }
                showImageInfo(mediaFile, parentView);
            }
        });
        mediaTaskList.add(task);
        if (mediaManager != null) {
            mediaManager.getScheduler().resume(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtil.showWarning("预览失败");
                    }
                    mediaManager.getScheduler().moveTaskToNext(task);
                }
            });
        } else {
            ToastUtil.showNormal("当前无法访问相册或无人机未连接");
            tvPhotoTime.setText("相册预览");
            setViewGone(pbLoading,false);
        }

    }

    private void showImageInfo(MediaFile mediaFile, View parentView) {
        currentTime = DateUtil.parseDateString("yyyy-MM-dd-HH:mm:ss", mediaFile.getTimeCreated());
        PhotoView photoView = parentView.findViewById(R.id.photoPreview);
        ImageView ivPlayVideo = parentView.findViewById(R.id.ivPlayVideo);
        LogUtils.i(TAG + "执行了3");
        boolean isPhoto = mediaFile.getMediaType() == MediaFile.MediaType.JPEG || mediaFile.getMediaType() == MediaFile.MediaType.RAW_DNG;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setViewGone(ivPlayVideo, !isPhoto);
                tvPhotoTime.setText(StringUtil.getNotNullValueLine(currentTime));
                try {
                    GlideManager.loadImgAuto(mediaFile.getPreview(), photoView);
                }catch (IllegalArgumentException e){
                    LogUtils.e(TAG+e.toString());
                }

                ivPlayVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipVideoPlay(mediaFile);
                    }
                });
            }
        });

    }

    private void setViewGone(View view, boolean visible) {
        if (view == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visible) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        });

    }

    private void initMediaManager() {
        if (ProductManager.getProductInstance() == null) {
            return;
        }
        Camera camera = ProductManager.getProductInstance().getCamera();
        if (!camera.isMediaDownloadModeSupported()) {
            ToastUtil.showWarning("当前机型不支持下载模式");
            return;
        }
        mediaManager = camera.getMediaManager();
    }

    private void skipVideoPlay(MediaFile mediaFile) {
        if (mediaFile == null) {
            ToastUtil.showWarning("未获取到当前视频");
            return;
        }
        if (MediaTemp.previewMediaFileList == null) {
            MediaTemp.previewMediaFileList = new ArrayList<>();
        }
        MediaTemp.previewMediaFileList.clear();
        MediaTemp.previewMediaFileList.addAll(mediaFileList);
        Intent intent = new Intent();
        intent.setClass(this, PlayVideoActivityNew.class);
        intent.putExtra(EXTRA_CREATE_TIME, mediaFile.getTimeCreated());
        startActivityForResult(intent, 3000);
    }

    private void releaseMediaManager() {
        if (mediaManager != null) {
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
            }
            if (bitmapCacheMap != null) {
                for (Map.Entry<Long, Bitmap> longBitmapEntry : bitmapCacheMap.entrySet()) {
                    if (longBitmapEntry.getValue() != null) {
                        longBitmapEntry.getValue().recycle();
                    }
                }
            }
        }
    }
}
