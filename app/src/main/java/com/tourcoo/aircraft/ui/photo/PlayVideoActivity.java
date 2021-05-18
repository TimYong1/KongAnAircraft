package com.tourcoo.aircraft.ui.photo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.util.ToastUtil;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import java.util.List;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.ux.beta.core.extension.ViewExtensions;

import static com.tourcoo.aircraft.ui.photo.MediaTemp.previewMediaFileList;
import static com.tourcoo.aircraft.ui.photo.PhotoPreviewActivityNew.EXTRA_CREATE_TIME;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月13日15:42
 * @Email: 971613168@qq.com
 */
public class PlayVideoActivity extends RxAppCompatActivity implements MediaManager.VideoPlaybackStateListener, View.OnClickListener {
    private MediaManager mediaManager;
    private ProgressDialog dialog;
    private boolean isDialogAllowable = false;
    private MediaFile.VideoPlaybackStatus mVideoPlaybackState;
    private ImageView ivPlayVideo, ivPlayPause;
    private MediaFile currentMedia;
    private long mediaCreateTime;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        mHandler = new Handler(Looper.getMainLooper());
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mediaCreateTime = getIntent().getLongExtra(EXTRA_CREATE_TIME, -1);
        if (mediaCreateTime < 0) {
            ToastUtil.showWarning("未获取到视频预览");
            finish();
        }
        ivPlayVideo = findViewById(R.id.ivPlayVideo);
        ivPlayPause = findViewById(R.id.ivPlayPause);
        ivPlayVideo.setOnClickListener(this);
        ivPlayPause.setOnClickListener(this);
        initMediaManager();
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
        loadMediaList(camera);
        if (mediaManager != null && mediaManager.isVideoPlaybackSupported()) {
            mediaManager.addMediaUpdatedVideoPlaybackStateListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigation();
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


    private void showVideoPreview() {
        if (mediaManager == null) {
            ToastUtil.showWarning("当前状态无法访问");
            return;
        }
        if (!mediaManager.isVideoPlaybackSupported()) {
            ToastUtil.showWarning("当前设备不支持视频回放功能");
            return;
        }
        if (currentMedia == null) {
            ToastUtil.showWarning("未找到对应视频");
            return;
        }
        doPlayVideo();
    }

    @Override
    public void onUpdate(MediaManager.VideoPlaybackState videoPlaybackState) {
        LogUtils.e("播放状态：" + videoPlaybackState);
        if (videoPlaybackState.getPlaybackStatus() == null) {
            return;
        }
        mVideoPlaybackState = videoPlaybackState.getPlaybackStatus();
        updateTextView(videoPlaybackState);
    }


    private void updateTextView(MediaManager.VideoPlaybackState currentVideoPlaybackState) {
        if (currentVideoPlaybackState == null) {
            ToastUtil.showWarning("当前播放异常");
            return;
        }
        if (currentVideoPlaybackState.getPlayingMediaFile() == null) {
            return;
        }
        switch (currentVideoPlaybackState.getPlaybackStatus()) {
            case STOPPED:
            case PAUSED:
                //当前状态为暂停状态 需要显示播放按钮
                showPlay();
                break;
            case PLAYING:
                showPause();
                break;
            default:
                break;
        }

    }


    private void createProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setMessage(this.getString(R.string.loading));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        ProgressDialog downloadDialog = new ProgressDialog(this);
        downloadDialog.setTitle(R.string.sync_file_title);
        downloadDialog.setIcon(android.R.drawable.ic_dialog_info);
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadDialog.setCanceledOnTouchOutside(false);
        downloadDialog.setCancelable(false);
    }

    private void showProgressDialog() {
        if (dialog != null && isDialogAllowable) {
            dialog.show();
        }
    }

    private void hideProgressDialog() {
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        isDialogAllowable = true;
        release();
        super.onDestroy();
    }

    private void release() {
        if (AircraftUtil.isCameraModuleAvailable() && ProductManager.getProductInstance().getCamera().isMediaDownloadModeSupported()) {
            mediaManager = ProductManager.getProductInstance().getCamera().getMediaManager();
            if (null != mediaManager) {
                if (mediaManager.isVideoPlaybackSupported()) {
                    mediaManager.removeMediaUpdatedVideoPlaybackStateListener(this);
                }
                mediaManager.stop(null);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivPlayVideo:
                showVideoPreview();
                break;
            case R.id.ivPlayPause:
                doPauseOrResume();
                break;
        }
    }

    private MediaFile findCurrentMediaAndCheck(List<MediaFile> mediaFiles) {
        if (mediaFiles == null || mediaFiles.size() == 0) {
            ToastUtil.showWarning("未获取到视频");
            finish();
        }
        MediaFile mediaFile;
        for (int i = 0; i < mediaFiles.size(); i++) {
            mediaFile = mediaFiles.get(i);
            LogUtils.i("视频时间="+mediaFile.getDateCreated());
            if (mediaFile != null && mediaCreateTime == mediaFile.getTimeCreated()) {
                return mediaFile;
            }
        }
        return null;
    }

    private void doPlayVideo() {
        if (mVideoPlaybackState == null) {
            play();
        } else {
            if (MediaFile.VideoPlaybackStatus.STOPPED == mVideoPlaybackState || MediaFile.VideoPlaybackStatus.UNKNOWN == mVideoPlaybackState) {
                //停止时 开始回放视频
                play();
            } else {
                doPauseOrResume();
            }
        }


    }

    private void play() {
        mediaManager.playVideoMediaFile(currentMedia, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtil.showWarningCondition("播放异常:" + djiError.getDescription(), "播放异常");
                } else {
                    ToastUtil.showSuccess("播放了视频");
                }
            }
        });
    }


    private void stop() {
        if (mediaManager == null) {
            ToastUtil.showWarning("当前状态无法播放");
            return;
        }
        mediaManager.stop(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtil.showWarningCondition("停止：" + djiError.getDescription(), "停止异常");
                } else {
                    //todo
                    ToastUtil.showSuccess("停止了播放");
                }
            }
        });
    }

    private void doPauseOrResume() {
        if (mediaManager == null) {
            ToastUtil.showWarning("视频操作异常");
            return;
        }
        //当视频暂停时 继续播放
        if (MediaFile.VideoPlaybackStatus.PAUSED == mVideoPlaybackState) {
            mediaManager.resume(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtil.showWarningCondition(djiError.getDescription(), "播放异常");
                    } else {
                        //todo
                        ToastUtil.showSuccess("恢复了播放");
                        showPause();
                    }
                }
            });
        }
        if (MediaFile.VideoPlaybackStatus.PLAYING == mVideoPlaybackState) {
            mediaManager.pause(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtil.showWarningCondition(djiError.getDescription(), "暂停异常");
                    } else {
                        //todo
                        ToastUtil.showSuccess("暂停了播放");
                        showPlay();
                    }
                }
            });
        }

    }

    private void showPause() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewExtensions.show(ivPlayPause);
                ViewExtensions.hide(ivPlayVideo);
            }
        });
    }


    private void showPlay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewExtensions.show(ivPlayVideo);
                ViewExtensions.hide(ivPlayPause);
            }
        });
    }


    private void loadMediaList(Camera camera) {
        if (camera == null) {
            ToastUtil.showWarning("当前相册不可用");
            return;
        }
        CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation> callbackWith = new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation>() {
            @Override
            public void onSuccess(SettingsDefinitions.StorageLocation storageLocation) {
                mediaManager.refreshFileListOfStorageLocation(storageLocation, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            List<MediaFile> medias = mediaManager.getSDCardFileListSnapshot();
                            currentMedia =      findCurrentMediaAndCheck(medias);
//                            Log.d(TAG, "fetchMediaList onSuccess");
//                            handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                         /*   if (DJIMediaList != null) {
                                DJIMediaList.clear();
                            }
                            for (MediaFile media : medias) {
                                DJIMediaList.add(media);
                            }
                            handler.sendMessage(handler.obtainMessage(NEED_REFRESH_FILE_LIST, null));
                            handler.removeMessages(GET_THUMBNAILS);*/
                        } else {
                            ToastUtil.showWarning("访问相册失败");
                            finish();
                           /* handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                            handler.sendMessage(handler.obtainMessage(SHOW_TOAST, djiError.getDescription()));*/
                        }
                    }
                });
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        };
        camera.getStorageLocation(callbackWith);
    }
}
