package com.tourcoo.aircraft.widget.camera;


import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.timer.OnCountDownTimerListener;
import com.tourcoo.timer.TimeTool;
import com.tourcoo.util.DateUtil;
import com.tourcoo.util.ToastUtil;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.ux.beta.core.extension.ViewExtensions;

/**
 * @author :JenkinsZhou
 * @description :媒体回放播放控件
 * @company :途酷科技
 * @date 2021年05月19日15:13
 * @Email: 971613168@qq.com
 */
public class PlaybackVideoView extends LinearLayout implements MediaManager.VideoPlaybackStateListener {
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int FETCH_FILE_LIST = 6;
    public ImageView ivPlayVideo, ivPlayPause;
    private BaseCameraView textureView;
    private TextView tvCurrentVideoTime, tvTotalVideoTime;
    private MediaManager mediaManager;
    private boolean isDialogAllowable = false;
    private ProgressBar pbLoading;
    private MediaFile.VideoPlaybackStatus mVideoPlaybackState;
    private MediaFile currentMedia;
    private float mVideoDuration;
    private long mMediaCreateTime;
    private Camera camera;
    private TimeTool timeTool;
    private boolean intercept = false;
    protected DismissControlViewTimerTask mDismissControlViewTimerTask;
    private Timer timer;
    private LinearLayout llPlayControl;
    public static final String TAG = "PlaybackVideoView";
    private SeekBar bottomSeekProgress;
    private Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS_DIALOG:
                    showProgressDialog();
                    break;
                case HIDE_PROGRESS_DIALOG:
                    hideProgressDialog();
                    break;
                case FETCH_FILE_LIST:
                    loadMediaList(camera);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    public PlaybackVideoView(Context context) {
        super(context);
        initUI(context);
        initTimer();
        initDJIMedia();
    }

    public PlaybackVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
        initTimer();
        initDJIMedia();
    }


    private void showProgressDialog() {
        if (pbLoading != null && isDialogAllowable) {
            ViewExtensions.show(pbLoading);
        }
    }

    private void hideProgressDialog() {
        if (null != pbLoading) {
            ViewExtensions.hide(pbLoading);
        }
    }


    private void initUI(Context context) {
        setOrientation(HORIZONTAL);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.media_play_back_layout, this, true);
        ivPlayVideo = findViewById(R.id.ivPlayVideo);
        textureView = findViewById(R.id.textureView);
        ivPlayPause = findViewById(R.id.ivPlayPause);
        pbLoading = findViewById(R.id.pbLoading);
        llPlayControl = findViewById(R.id.llPlayControl);
        tvCurrentVideoTime = findViewById(R.id.tvCurrentVideoTime);
        tvTotalVideoTime = findViewById(R.id.tvTotalVideoTime);
        bottomSeekProgress = findViewById(R.id.bottomSeekProgress);
        ivPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doPauseOrResume();
            }
        });

        ivPlayVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideoPreview();
            }
        });
        textureView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                control();
            }
        });

    }

    private boolean initDJIMedia() {
        BaseProduct product;
        try {
            product = ProductManager.getProductInstance();
        } catch (Exception exception) {
            product = null;
        }
        if (product == null) {
            ToastUtil.showWarning("无人机断开连接");
            return false;
        } else {
            if (null != ProductManager.getProductInstance().getCamera()
                    && ProductManager.getProductInstance().getCamera().isMediaDownloadModeSupported()) {
                camera = ProductManager.getProductInstance().getCamera();
                camera.enterPlayback(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            mediaManager = camera.getMediaManager();
                            if (null != mediaManager) {
                                if (mediaManager.isVideoPlaybackSupported()) {
                                    mediaManager.addMediaUpdatedVideoPlaybackStateListener(PlaybackVideoView.this);
                                }
                            }
                            handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS_DIALOG, null));
                            handler.sendMessageDelayed(handler.obtainMessage(FETCH_FILE_LIST, null),
                                    1000);
                        } else {
                            ToastUtil.showWarning("访问相册失败");
                        }
                    }
                });

            } else if (null != ProductManager.getProductInstance().getCamera()
                    && !ProductManager.getProductInstance().getCamera().isMediaDownloadModeSupported()) {
                ToastUtil.showWarning("不支持媒体预览模式");
                return false;
            } else {
                ToastUtil.showWarning("无人机已断开");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isDialogAllowable = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        CameraHelper.getInstance().setCameraModePhotoSingle();
        release();
        super.onDetachedFromWindow();
    }

    @Override
    public void onUpdate(MediaManager.VideoPlaybackState videoPlaybackState) {
        if (videoPlaybackState == null) {
            return;
        }
        if (intercept) {
            return;
        }
        mVideoPlaybackState = videoPlaybackState.getPlaybackStatus();
        updateTextView(videoPlaybackState);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                ViewExtensions.show(ivPlayPause);
                ViewExtensions.hide(ivPlayVideo);
            }
        });
    }


    private void showPlay() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ViewExtensions.show(ivPlayVideo);
                ViewExtensions.hide(ivPlayPause);
            }
        });
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
        boolean isVideo = currentMedia.getMediaType() == MediaFile.MediaType.MOV || (currentMedia.getMediaType() == MediaFile.MediaType.MP4);
        if (!isVideo) {
            ToastUtil.showWarning("当前格式不支持");
            return;
        }
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
                            currentMedia = findCurrentMediaAndCheck(medias);
                            handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                            loadVideoInfo(currentMedia);
                        } else {
                            ToastUtil.showWarning("访问相册失败");
                            //todo
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

    private MediaFile findCurrentMediaAndCheck(List<MediaFile> mediaFiles) {
        if (mediaFiles == null || mediaFiles.size() == 0) {
            ToastUtil.showWarning("未获取到视频");
            //todo
            return null;
        }
        MediaFile mediaFile;
        for (int i = 0; i < mediaFiles.size(); i++) {
            mediaFile = mediaFiles.get(i);
            String pattern = "yyyy-MM-dd-HH:mm:ss";
            LogUtils.e(TAG + "正在寻找mMediaCreateTime=" + mMediaCreateTime + "---->" + mediaFile.getTimeCreated() + "<----" + DateUtil.parseDateString(pattern, mediaFile.getTimeCreated()) + "对应的视频");
            if (mediaFile != null && mMediaCreateTime == mediaFile.getTimeCreated()) {
                return mediaFile;
            }
        }
        return null;
    }

    private void loadVideoInfo(MediaFile mediaFile) {
        if (mediaFile != null) {
            mVideoDuration = mediaFile.getDurationInSeconds();
            ToastUtil.showSuccess("当前视频日期：" + DateUtil.stringForTime(mediaFile.getTimeCreated()) + "");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tvTotalVideoTime.setText(DateUtil.stringForTime(mVideoDuration));
                }
            });
        } else {
            ToastUtil.showWarning("未找到对应媒体源");
        }
    }


    private void initTimer() {
        if (timeTool == null) {
            timeTool = new TimeTool(Integer.MAX_VALUE, 1000);
        } else {
            timeTool.reset();
        }
        timeTool.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                intercept = false;
                LogUtils.i(TAG + "拦截取消");
            }

            @Override
            public void onFinish() {
                timeTool.reset();
                timeTool.start();
            }

            @Override
            public void onCancel() {
                LogUtils.d("");
            }
        });
        timeTool.start();
    }


    public class DismissControlViewTimerTask extends TimerTask {
        @Override
        public void run() {
            dismissControlView();
        }
    }


    public void cancelDismissControlViewTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
        }

    }

    public void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        timer = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        timer.schedule(mDismissControlViewTimerTask, 2500);
    }


    private void updateTextView(MediaManager.VideoPlaybackState currentVideoPlaybackState) {
        if (currentVideoPlaybackState == null) {
            ToastUtil.showWarning("当前播放异常");
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoDuration > 0) {
                    float percent = currentVideoPlaybackState.getPlayingPosition() / mVideoDuration;
                    LogUtils.i(TAG + "percent=" + currentVideoPlaybackState.getPlayingPosition());
                    int progress = (int) (percent * 100);
                    LogUtils.i(TAG + "百分比=" + progress);
                    bottomSeekProgress.setProgress(progress);
                }
                tvCurrentVideoTime.setText(DateUtil.stringForTime(currentVideoPlaybackState.getPlayingPosition()));
                LogUtils.i(TAG + "百分比=PlayingPosition=" + currentVideoPlaybackState.getPlayingPosition());
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
                intercept = true;
            }
        });
    }

    public void dismissControlView() {
        if (MediaFile.VideoPlaybackStatus.PLAYING == mVideoPlaybackState) {
            ViewExtensions.hide(llPlayControl);
        }
    }


    private void control() {
        if (MediaFile.VideoPlaybackStatus.PLAYING != mVideoPlaybackState) {
            ViewExtensions.show(llPlayControl);
        } else {
            //如果在播放状态 则根据当前状态 显示或隐藏

            switch (llPlayControl.getVisibility()) {
                case View.GONE:
                case View.INVISIBLE:
                    ViewExtensions.show(llPlayControl);
                    break;
                case View.VISIBLE:
                    ViewExtensions.hide(llPlayControl);
                    break;
                default:
                    break;
            }
            startDismissControlViewTimer();
        }
    }


    private void release() {
        cancelDismissControlViewTimer();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (timeTool != null) {
            timeTool.stop();
            timeTool = null;
        }
        if (AircraftUtil.isCameraModuleAvailable() && ProductManager.getProductInstance().getCamera().isMediaDownloadModeSupported()) {
            mediaManager = ProductManager.getProductInstance().getCamera().getMediaManager();
            if (null != mediaManager) {
                if (mediaManager.isVideoPlaybackSupported()) {
                    mediaManager.removeMediaUpdatedVideoPlaybackStateListener(this);
                }
            }
        }
    }

    public void setMediaCreateTime(long time) {
        mMediaCreateTime = time;
    }
}
