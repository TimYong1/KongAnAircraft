package com.tourcoo.live;

import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

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
    private LiveListener liveListener;
    public static final int LIVE_OPEN_SUCCESS = 0;
    private final List<LiveListener> liveListenerList = new ArrayList<>();

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
            ToastUtil.showWarning("推流地址有误");
            return;
        }
        if (isLiveShowOpen()) {
//            Toast.makeText(context, "当前直播流已经开启", Toast.LENGTH_SHORT).show();
            ToastUtil.showWarning("当前直播流已经开启");
            return;
        }
        ThreadManager.getDefault().execute(() -> {
            DJISDKManager.getInstance().getLiveStreamManager().setLiveUrl(liveShowUrl);
            int result = DJISDKManager.getInstance().getLiveStreamManager().startStream();
            DJISDKManager.getInstance().getLiveStreamManager().setAudioMuted(true);
            DJISDKManager.getInstance().getLiveStreamManager().setStartTime();
            LogUtils.d(TAG + "直播流开启结果=" + result +
                    "\n isVideoStreamSpeedConfigurable:" + DJISDKManager.getInstance().getLiveStreamManager().isVideoStreamSpeedConfigurable() +
                    "\n isLiveAudioEnabled:" + DJISDKManager.getInstance().getLiveStreamManager().isLiveAudioEnabled());
            if (LIVE_OPEN_SUCCESS == result) {
                isLiveShowOpen = true;
            } else {
                isLiveShowOpen = false;
            }
            if (liveListener != null) {
                liveListener.liveOpenResult(result);
            }
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
        if (!isLiveShowOpen) {
            isLiveShowOpen = false;
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().stopStream();
        if (liveListener != null) {
            liveListener.liveStop();
        }
        isLiveShowOpen = false;
    }

    public boolean isLiveShowOpen() {
        return isLiveStreamManagerOn() && DJISDKManager.getInstance().getLiveStreamManager().isStreaming()&& isLiveShowOpen;
    }

    private void soundOff() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().setAudioMuted(false);
    }

    private void unRegister() {
        if (DJISDKManager.getInstance().getLiveStreamManager() != null) {
            LiveListener listener;
            try {
                for (int i = liveListenerList.size() - 1; i >= 0; i--) {
                    listener = liveListenerList.get(i);
                    liveListenerList.remove(listener);
                    listener = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG + e.toString());
            }
        }
    }

    public void setLiveListener(LiveListener liveListener) {
        if (DJISDKManager.getInstance().getLiveStreamManager() == null) {
            LogUtils.e(TAG + "监听已拦截");
            return;
        }
        if (liveListener == null) {
            LogUtils.e(TAG + "监听已拦截");
            return;
        }
        this.liveListener = liveListener;
        liveListenerList.add(liveListener);
        LogUtils.i(TAG + "监听已添加");

    }

    public void release() {
        unRegister();
    }
}
