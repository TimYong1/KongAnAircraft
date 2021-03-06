package com.tourcoo.aircraft.ui.photo;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;

import com.tourcoo.aircraft.widget.camera.PlaybackVideoView;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.util.ToastUtil;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import static com.tourcoo.aircraft.ui.photo.PhotoPreviewActivityNew.EXTRA_CREATE_TIME;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月19日16:38
 * @Email: 971613168@qq.com
 */
public class PlayVideoActivityNew extends RxAppCompatActivity {
    private long mediaCreateTime;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video_new);
        mediaCreateTime = getIntent().getLongExtra(EXTRA_CREATE_TIME, -1);
        if (mediaCreateTime < 0) {
            ToastUtil.showWarning("未获取到视频预览");
            finish();
        }
        PlaybackVideoView playbackVideoView = findViewById(R.id.playBackView);
        playbackVideoView.setMediaCreateTime(mediaCreateTime);
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
}
