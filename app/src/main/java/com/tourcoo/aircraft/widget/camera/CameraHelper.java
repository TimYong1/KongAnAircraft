package com.tourcoo.aircraft.widget.camera;


import android.os.Handler;
import android.os.Looper;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.util.ToastUtil;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;

/**
 * @author :JenkinsZhou
 * @description : 相机相关控制类
 * @company :途酷科技
 * @date 2021年03月25日17:24
 * @Email: 971613168@qq.com
 */
public class CameraHelper {
    public static final String TAG = "CameraHelper";
    private int currentState;
    private Handler mHandler;

    /**
     * 设置拍照模式
     */
    public void setCameraModePhotoSingle(CommonCallbacks.CompletionCallback callback) {
        if (!AircraftUtil.isCameraModuleAvailable()) {
            ToastUtil.showNormal("无人机未连接或相机不可用");
            return;
        }
        if (AircraftUtil.isMavicAir2()) {
            ProductManager.getProductInstance().getCamera().setFlatMode(SettingsDefinitions.FlatCameraMode.PHOTO_SINGLE, callback);
        } else if (isNormalAircraft()) {
            ProductManager.getProductInstance().getCamera().setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, callback);
        } else {
            showTipsNotSupport();
        }
    }

    /**
     * 录像模式
     *
     * @param callback
     */
    public void setCameraModeRecord(CommonCallbacks.CompletionCallback callback) {
        if (!AircraftUtil.isCameraModuleAvailable()) {
            ToastUtil.showNormal("无人机未连接或相机不可用");
            return;
        }
        if (AircraftUtil.isMavicAir2()) {
            LogUtils.d("当前机器是MavicAir2");
            ProductManager.getProductInstance().getCamera().setFlatMode(SettingsDefinitions.FlatCameraMode.VIDEO_NORMAL, callback);
        } else {
            ProductManager.getProductInstance().getCamera().setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO, callback);
        }

    }

    public void takePhoto(CommonCallbacks.CompletionCallback completionCallback) {
        if (!AircraftUtil.isCameraModuleAvailable()) {
            ToastUtil.showNormal("无人机未连接或相机不可用");
            return;
        }
        ProductManager.getProductInstance().getCamera().startShootPhoto(completionCallback);
    }

    /**
     * 开始录制
     */
    public void startRecord(CommonCallbacks.CompletionCallback completionCallback) {
        if (!AircraftUtil.isCameraModuleAvailable()) {
            ToastUtil.showNormal("无人机未连接或相机不可用");
            return;
        }
        ProductManager.getProductInstance().getCamera().startRecordVideo(completionCallback);
    }


    /**
     * 停止录制
     */
    public void stopRecord(CommonCallbacks.CompletionCallback completionCallback) {
        if (!AircraftUtil.isCameraModuleAvailable()) {
            ToastUtil.showNormal("无人机未连接或相机不可用");
            return;
        }
        ProductManager.getProductInstance().getCamera().stopRecordVideo(completionCallback);
    }

    private static class Holder {
        private static final CameraHelper instance = new CameraHelper();
    }

    private CameraHelper() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static CameraHelper getInstance() {
        return Holder.instance;
    }

    public void runUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    private void showTipsNotSupport() {
        runUiThread(() -> {
            ToastUtil.showWarningDebug("当前机型不支持该指令");
        });
    }


    public void setCameraModePhotoSingle() {
        if (!AircraftUtil.isCameraModuleAvailable()) {
            return;
        }
        setCameraModePhotoSingle(null);
    }


    private boolean isNormalAircraft() {
        return AircraftUtil.isMatchModel(Model.MAVIC_MINI) || AircraftUtil.isMatchModel(Model.MAVIC_2_ENTERPRISE_DUAL) || AircraftUtil.isMatchModel(Model.MATRICE_300_RTK);
    }

    private boolean isSpecialAircraft() {
        return AircraftUtil.isMatchModel(Model.MAVIC_AIR_2);
    }


    public Camera getCamera() {
        if (!AircraftUtil.isCameraModuleAvailable()) {
            return null;
        }
        return AircraftUtil.getAircraftInstance().getCamera();
    }
}
