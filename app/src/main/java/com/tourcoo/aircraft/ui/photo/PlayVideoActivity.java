package com.tourcoo.aircraft.ui.photo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.util.ToastUtil;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

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
    private MediaManager.VideoPlaybackState mVideoPlaybackState;
    private ImageView ivPlayVideo;
    private MediaFile currentMedia;
    private long mediaCreateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
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
        ivPlayVideo.setOnClickListener(this);
        currentMedia = findCurrentMediaAndCheck();
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
        playVideo();
    }

    @Override
    public void onUpdate(MediaManager.VideoPlaybackState videoPlaybackState) {
        updateTextView(videoPlaybackState);
    }


    private void updateTextView(MediaManager.VideoPlaybackState currentVideoPlaybackState) {
//        final StringBuilder pushInfo = new StringBuilder();
        if (currentVideoPlaybackState != null) {
            if (currentVideoPlaybackState.getPlayingMediaFile() != null) {
           /*     addLineToSB(pushInfo, "media index", currentVideoPlaybackState.getPlayingMediaFile().getIndex());
                addLineToSB(pushInfo, "media size", currentVideoPlaybackState.getPlayingMediaFile().getFileSize());
                addLineToSB(pushInfo,
                        "media duration",
                        currentVideoPlaybackState.getPlayingMediaFile().getDurationInSeconds());
                addLineToSB(pushInfo,
                        "media created date",
                        currentVideoPlaybackState.getPlayingMediaFile().getDateCreated());
                addLineToSB(pushInfo,
                        "media orientation",
                        currentVideoPlaybackState.getPlayingMediaFile().getVideoOrientation());*/
            } else {
//                addLineToSB(pushInfo, "media index", "None");
            }
//            addLineToSB(pushInfo, "media current position", currentVideoPlaybackState.getPlayingPosition());
//            addLineToSB(pushInfo, "media current status", currentVideoPlaybackState.getPlaybackStatus());
//            pushInfo.append("\n");
//            setResultToText(pushInfo.toString());
        } else {
//            setResultToText("playbackState is null");
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
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivPlayVideo:
                showVideoPreview();
                break;
        }
    }

    private MediaFile findCurrentMediaAndCheck() {
        if (previewMediaFileList == null || previewMediaFileList.size() == 0) {
            ToastUtil.showWarning("未获取到视频");
            finish();
        }
        MediaFile mediaFile;
        for (int i = 0; i < previewMediaFileList.size(); i++) {
            mediaFile = previewMediaFileList.get(i);
            if (mediaFile != null && mediaCreateTime == mediaFile.getTimeCreated()) {
                return mediaFile;
            }
        }
        return null;
    }

    private void playVideo() {
        mediaManager.playVideoMediaFile(currentMedia, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtil.showWarningCondition(djiError.getDescription(), "播放异常");
                }else {
                    ToastUtil.showSuccess("播放了视频");
                }

            }
        });
    }
}
