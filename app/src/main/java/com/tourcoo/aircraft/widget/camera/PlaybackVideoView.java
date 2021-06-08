package com.tourcoo.aircraft.widget.camera;


import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.tourcoo.aircraft.ui.photo.MediaTemp;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.timer.OnCountDownTimerListener;
import com.tourcoo.timer.TimeTool;
import com.tourcoo.util.DateUtil;
import com.tourcoo.util.ToastUtil;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    public static final String TIPS_DEVICE_ERROR = "设备繁忙或以断开";
    public ImageView ivPlayVideo, ivPlayPause, ivPreview;
    private BaseCameraView baseCameraView;
    private TextView tvCurrentVideoTime, tvTotalVideoTime;
    private MediaManager mediaManager;
    private boolean isDialogAllowable = false;
    private ProgressBar pbLoading;
    private MediaFile.VideoPlaybackStatus mVideoPlaybackStatus;
    private MediaManager.VideoPlaybackState mVideoPlaybackStates;
    private MediaFile.VideoPlaybackStatus lastPlayStatus;
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
    private SeekBar seekBar;
    private Handler handler = new Handler(Looper.getMainLooper());
    /**
     * 用户是否刚拖动过进度条
     */
    private boolean userSeek = false;

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
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (pbLoading != null && isDialogAllowable) {
                    ViewExtensions.show(pbLoading);
                }
            }
        });

    }

    private void hideProgressDialog() {
        if (null != pbLoading) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ViewExtensions.hide(pbLoading);
                }
            });

        }
    }


    private void initUI(Context context) {
        setOrientation(HORIZONTAL);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.media_play_back_layout, this, true);
        ivPlayVideo = findViewById(R.id.ivPlayVideo);
        baseCameraView = findViewById(R.id.textureView);
        ivPlayPause = findViewById(R.id.ivPlayPause);
        pbLoading = findViewById(R.id.pbLoading);
        llPlayControl = findViewById(R.id.llPlayControl);
        tvCurrentVideoTime = findViewById(R.id.tvCurrentVideoTime);
        tvTotalVideoTime = findViewById(R.id.tvTotalVideoTime);
        ivPreview = findViewById(R.id.ivPreview);
        seekBar = findViewById(R.id.bottomSeekProgress);
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
        baseCameraView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                control();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //不管是否是处于播放状态 这里都需要暂停播放
                //先获取之前的播放状态
                lastPlayStatus = mVideoPlaybackStates.getPlaybackStatus();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //这里说明 用户主动拖动进度条
                userSeek = true;
                if (MediaFile.VideoPlaybackStatus.PAUSED == lastPlayStatus) {
                    //说明本来就是暂停状态
                    LogUtils.i(TAG + "本来就是暂停状态");
                } else if (MediaFile.VideoPlaybackStatus.PLAYING == lastPlayStatus) {
                    //说明之前是
                    LogUtils.i(TAG + "上次是播放状态 需要跳转播放");
                    playResume();
                    playToPosition(computeVideoProgress(seekBar.getProgress()));
                }
            }
        });
        showProgressDialog();
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
                            currentMedia = findCurrentMediaAndCheck(MediaTemp.previewMediaFileList);
                            loadVideoInfo(currentMedia);
                          /*  handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS_DIALOG, null));
                            handler.sendMessageDelayed(handler.obtainMessage(FETCH_FILE_LIST, null),
                                    1000);*/
                        } else {
                            ToastUtil.showWarning(TIPS_DEVICE_ERROR);
                            hideProgressDialog();
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
        mVideoPlaybackStates = videoPlaybackState;
        mVideoPlaybackStatus = videoPlaybackState.getPlaybackStatus();
        float percent = (videoPlaybackState.getPlayingPosition() / mVideoDuration) * 100;
        if (percent >= 100) {
            handlePlayComplete();
        }
        if (intercept) {
            return;
        }
        updateTextView(videoPlaybackState, percent);
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
        if (MediaFile.VideoPlaybackStatus.PAUSED == mVideoPlaybackStatus) {
            if(userSeek){
                playToPosition(computeVideoProgress(seekBar.getProgress()));
            }else {
                playResume();
            }
        }
        if (MediaFile.VideoPlaybackStatus.PLAYING == mVideoPlaybackStatus) {
            playPause();
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
        if (mVideoPlaybackStatus == null) {
            play();
        } else {
            if (MediaFile.VideoPlaybackStatus.STOPPED == mVideoPlaybackStatus || MediaFile.VideoPlaybackStatus.UNKNOWN == mVideoPlaybackStatus) {
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
                    timeTool.resume();
                }
            }
        });

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
            if (mediaFile != null && mMediaCreateTime == mediaFile.getTimeCreated()) {
                return mediaFile;
            }
        }
        return null;
    }

    private void loadVideoInfo(MediaFile mediaFile) {
        hideProgressDialog();
        if (mediaFile != null) {
            mVideoDuration = mediaFile.getDurationInSeconds();
            ivPreview.setImageBitmap(mediaFile.getPreview());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tvTotalVideoTime.setText(DateUtil.stringForTime(mVideoDuration));
                    showVideoPreview();
                }
            });
        } else {
            ToastUtil.showWarning("未找到对应媒体源");
        }
    }


    private void initTimer() {
        if (timeTool == null) {
            timeTool = new TimeTool(Integer.MAX_VALUE, 400);
        } else {
            timeTool.reset();
        }
        timeTool.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                intercept = false;
//                checkPlayComplete();
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


    private void updateTextView(MediaManager.VideoPlaybackState currentVideoPlaybackState, float progress) {
        if (currentVideoPlaybackState == null) {
            ToastUtil.showWarning("当前播放异常");
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoDuration > 0) {
                    seekBar.setProgress((int) progress);
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
                        ViewExtensions.hide(ivPreview);
                        break;
                    default:
                        break;
                }
                intercept = true;
            }
        });
    }

    public void dismissControlView() {
        if (MediaFile.VideoPlaybackStatus.PLAYING == mVideoPlaybackStatus) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ViewExtensions.hide(llPlayControl);
                }
            });

        }
    }


    private void control() {
        if (MediaFile.VideoPlaybackStatus.PLAYING != mVideoPlaybackStatus) {
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
                    mediaManager.stop(null);
                    mediaManager.removeMediaUpdatedVideoPlaybackStateListener(this);
                }
            }
        }
    }

    public void setMediaCreateTime(long time) {
        mMediaCreateTime = time;
    }

    /**
     * 恢复播放
     */
    public void playResume() {
        mediaManager.resume(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtil.showWarningCondition(djiError.getDescription(), "播放异常");
                } else {
                    //todo
                    timeTool.reset();
                   initTimer();
                    showPause();
                }
            }
        });
    }

    /**
     * 暂停播放
     */
    public void playPause() {
        mediaManager.pause(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtil.showWarningCondition(djiError.getDescription(), "暂停异常");
                } else {
                    //todo
                    timeTool.pause();
                    showPlay();
                }
            }
        });
    }

    /**
     * 播放完成
     */
    private void handlePlayComplete() {
        if (mediaManager != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showSuccessDebug("播放完成");
                    LogUtils.i(TAG + "执行了handlePlayComplete");
                    timeTool.pause();
                    tvCurrentVideoTime.setText(DateUtil.stringForTime(mVideoDuration));
                    mediaManager.stop(null);
                    seekBar.setProgress(100);
                    showPlay();
                }
            });

        }
    }

    private void playToPosition(float videoPosition) {
        if (mediaManager != null) {
            mediaManager.moveToPosition(videoPosition, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtil.showWarningCondition("跳转播放失败:" + djiError.getDescription(), "跳转播放失败");
                        return;
                    }
                    //跳转播放成功 将拖动状态置为false
                    userSeek = false;
                    timeTool.reset();
                    showPause();
                }
            });
        }
    }

    private float computeVideoProgress(int seekProgress) {
        float per = seekProgress / 100f;
        return per * mVideoDuration;
    }

    private void pauseNoShowUi() {
        mediaManager.pause(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    timeTool.pause();
                    showPlay();
                }
            }
        });
    }
}
