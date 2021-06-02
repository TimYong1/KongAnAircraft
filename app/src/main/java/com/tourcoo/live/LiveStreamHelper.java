package com.tourcoo.live;

import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.util.ToastUtil;

import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;

/**
 * @author :JenkinsZhou
 * @description : 直播流相关管理类
 * @company :途酷科技
 * @date 2021年03月18日17:13
 * @Email: 971613168@qq.com
 */
public class LiveStreamHelper {
    private static final String TAG = "LiveStreamHelper";
    //    private String liveShowUrl = "";
//    private Context context = FlyApplication.getContext();
    private boolean isLiveShowOpen = false;

    public boolean isLiveStreamManagerOn() {
        if (DJISDKManager.getInstance().getLiveStreamManager() == null) {
            LogUtils.w(TAG + "isLiveStreamManagerOn=" + false);
            return false;
        }
        return true;
    }

    private boolean isSupportSecondaryVideo() {
        if (!Helper.isMultiStreamPlatform()) {
            LogUtils.w(TAG + "isSupportSecondaryVideo=" + false);
            return false;
        }
        return true;
    }


    public void startLiveShow(String liveShowUrl) {
        if (TextUtils.isEmpty(liveShowUrl)) {
            LogUtils.e(TAG + "推流地址有误");
            return;
        }
        if (!isLiveStreamManagerOn()) {
//            Toast.makeText(context, "当前直播流已经开启", Toast.LENGTH_SHORT).show();
            ToastUtil.showWarning("当前直播流已经开启");
            return;
        }
        if (DJISDKManager.getInstance().getLiveStreamManager().isStreaming()) {
            LogUtils.i(TAG + "直播流已经开启");
            isLiveShowOpen = true;
            ToastUtil.showNormal("当前直播正在进行");
            return;
        }
        isLiveShowOpen = true;
        ThreadManager.getDefault().execute(() -> {
            DJISDKManager.getInstance().getLiveStreamManager().setLiveUrl(liveShowUrl);
            int result = DJISDKManager.getInstance().getLiveStreamManager().startStream();
            DJISDKManager.getInstance().getLiveStreamManager().registerListener(new LiveStreamManager.OnLiveChangeListener() {
                @Override
                public void onStatusChanged(int i) {

                }
            });
            DJISDKManager.getInstance().getLiveStreamManager().setAudioMuted(true);
            DJISDKManager.getInstance().getLiveStreamManager().setStartTime();
            LogUtils.d(TAG + "直播流开启结果=" + result +
                    "\n isVideoStreamSpeedConfigurable:" + DJISDKManager.getInstance().getLiveStreamManager().isVideoStreamSpeedConfigurable() +
                    "\n isLiveAudioEnabled:" + DJISDKManager.getInstance().getLiveStreamManager().isLiveAudioEnabled());
            ToastUtil.showNormal("直播已开启");
        });

    }

    private void enableReEncoder() {
        if (!isLiveStreamManagerOn()) {
            LogUtils.d(TAG + "直播流未开启!");
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().setVideoEncodingEnabled(true);
        LogUtils.d(TAG + "Force Re-Encoder Enabled!");
    }

    private void disableReEncoder() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().setVideoEncodingEnabled(false);
        LogUtils.d(TAG + "disableReEncoder");
    }


    private static class SingleHolder {
        private static LiveStreamHelper instance = new LiveStreamHelper();
    }

    private LiveStreamHelper() {

    }

    public static LiveStreamHelper getInstance() {
        return SingleHolder.instance;
    }

    public void stopLiveShow() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        if (!isLiveShowOpen) {
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().stopStream();
        isLiveShowOpen = false;
        ToastUtil.showNormal("直播已关闭");
    }

    public boolean isLiveShowOpen() {
        return isLiveShowOpen;
    }

    private void soundOff() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().setAudioMuted(false);
    }
}
