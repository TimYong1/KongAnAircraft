package com.tourcoo.aircraft.widget.gimble;


import android.os.Handler;
import android.os.Looper;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.util.ToastUtil;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.gimbal.CapabilityKey;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.common.util.DJIParamCapability;
import dji.common.util.DJIParamMinMaxCapability;
import dji.sdk.base.BaseProduct;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

import static dji.common.gimbal.CapabilityKey.ADJUST_PITCH;
import static dji.common.gimbal.CapabilityKey.ADJUST_ROLL;
import static dji.common.gimbal.CapabilityKey.ADJUST_YAW;

/**
 * @author :JenkinsZhou
 * @description : 云台（万向轴）控制类
 * @company :途酷科技
 * @date 2021年03月26日13:45
 * @Email: 971613168@qq.com
 */
public class GimHelper {
    public static final String TAG = "GimHelper";
    private Gimbal gimbal = null;
    private int currentGimId = 0;
    private Timer timer;
    private GimRotateTimerTask gimRotateTimerTask;
    private Handler mHandler;

    private Gimbal getGimInstance() {
        if (gimbal == null) {
            initGim();
        }
        return gimbal;
    }


    /**
     * 计算梯度
     */
    private float getGradientVertical() {
        gimbal = getGimInstance();
        if (gimbal == null) {
            return -1;
        }
        if (gimbal.getCapabilities() == null) {
            return -1;
        }
        Map<CapabilityKey, DJIParamCapability> capabilityMap = gimbal.getCapabilities();
        if (capabilityMap == null) {
            return -1;
        }
        DJIParamMinMaxCapability yawCap = (DJIParamMinMaxCapability) capabilityMap.get(ADJUST_PITCH);
        if (yawCap == null) {
            return -1;
        }
        Number minValue = yawCap.getMin();
        Number maxValue = yawCap.getMax();
        if (minValue == null || maxValue == null) {
            return -1;
        }
        LogUtils.i(TAG + minValue + "maxValue=" + maxValue);
        return (Math.abs(minValue.floatValue()) + Math.abs(maxValue.floatValue())) / 20f;
    }


    private float getGradientHorizon() {
        gimbal = getGimInstance();
        if (gimbal == null) {
            return -1;
        }
        if (gimbal.getCapabilities() == null) {
            return -1;
        }
        Map<CapabilityKey, DJIParamCapability> capabilityMap = gimbal.getCapabilities();
        if (capabilityMap == null) {
            return -1;
        }
        DJIParamMinMaxCapability yawCap = (DJIParamMinMaxCapability) capabilityMap.get(ADJUST_YAW);
        if (yawCap == null) {
            return -1;
        }
        Number minValue = yawCap.getMin();
        Number maxValue = yawCap.getMax();
        if (minValue == null || maxValue == null) {
            return -1;
        }
        LogUtils.i(TAG + minValue + "maxValue=" + maxValue);
        return (Math.abs(minValue.floatValue()) + Math.abs(maxValue.floatValue())) / 20f;
    }

    private float getGradientRoll() {
        gimbal = getGimInstance();
        if (gimbal == null) {
            return -1;
        }
        if (gimbal.getCapabilities() == null) {
            return -1;
        }
        Map<CapabilityKey, DJIParamCapability> capabilityMap = gimbal.getCapabilities();
        if (capabilityMap == null) {
            return -1;
        }
        DJIParamMinMaxCapability rollCap = (DJIParamMinMaxCapability) capabilityMap.get(ADJUST_ROLL);
        if (rollCap == null) {
            return -1;
        }
        Number minValue = rollCap.getMin();
        Number maxValue = rollCap.getMax();
        if (minValue == null || maxValue == null) {
            return -1;
        }
        LogUtils.i(TAG + minValue + "maxValue=" + maxValue);
        return (Math.abs(minValue.floatValue()) + Math.abs(maxValue.floatValue())) / 20f;
    }

    private void initGim() {
        if (DJISDKManager.getInstance() != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null) {
                if (product instanceof Aircraft) {
                    gimbal = ((Aircraft) product).getGimbals().get(currentGimId);
                } else {
                    gimbal = product.getGimbal();
                }
            }
        }
    }


    private static class Holder {
        private static final GimHelper instance = new GimHelper();
    }

    private GimHelper() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static GimHelper getInstance() {
        return Holder.instance;
    }


    private static class GimRotateTimerTask extends TimerTask {
        float pitchValue;

        GimRotateTimerTask(float pitchValue) {
            super();
            this.pitchValue = pitchValue;
        }

        @Override
        public void run() {
            if (AircraftUtil.isGimbalModuleAvailable()) {
                ProductManager.getProductInstance().getGimbal().
                        rotate(new Rotation.Builder().pitch(pitchValue)
                                .mode(RotationMode.SPEED)
                                .yaw(Rotation.NO_ROTATION)
                                .roll(Rotation.NO_ROTATION)
                                .time(0)
                                .build(), new CommonCallbacks.CompletionCallback() {

                            @Override
                            public void onResult(DJIError error) {

                            }
                        });
            }
        }
    }


    private void controlUpAndDown(float verticalGradient) {
        ThreadManager.getDefault().execute(new Runnable() {
            @Override
            public void run() {
                if (AircraftUtil.isGimbalModuleAvailable()) {
                    ProductManager.getProductInstance().getGimbal().
                            rotate(new Rotation.Builder().pitch(verticalGradient)
                                    .mode(RotationMode.SPEED)
                                    .yaw(Rotation.NO_ROTATION)
                                    .roll(Rotation.NO_ROTATION)
                                    .time(0)
                                    .build(), new CommonCallbacks.CompletionCallback() {

                                @Override
                                public void onResult(DJIError error) {

                                }
                            });
                }
            }
        });

    }


    private void controlLeftAndRight(float horizonGradient) {
        ThreadManager.getDefault().execute(new Runnable() {
            @Override
            public void run() {
                if (AircraftUtil.isGimbalModuleAvailable()) {
                    ProductManager.getProductInstance().getGimbal().
                            rotate(new Rotation.Builder().yaw(horizonGradient)
                                    .mode(RotationMode.SPEED)
                                    .yaw(horizonGradient)
                                    .roll(Rotation.NO_ROTATION)
                                    .pitch(Rotation.NO_ROTATION)
                                    .time(2)
                                    .build(), new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError error) {

                                }
                            });
                }
            }
        });

    }

    private void controlRoll(float rollGradient) {
        ThreadManager.getDefault().execute(new Runnable() {
            @Override
            public void run() {
                if (AircraftUtil.isGimbalModuleAvailable()) {
                    ProductManager.getProductInstance().getGimbal().
                            rotate(new Rotation.Builder().roll(rollGradient)
                                    .mode(RotationMode.SPEED)
                                    .yaw(Rotation.NO_ROTATION)
                                    .roll(rollGradient)
                                    .pitch(Rotation.NO_ROTATION)
                                    .time(2)
                                    .build(), new CommonCallbacks.CompletionCallback() {

                                @Override
                                public void onResult(DJIError error) {

                                }
                            });
                }
            }
        });

    }

    public void gimUp() {
        if (!AircraftUtil.isProductModuleAvailable()) {
            ToastUtil.showWarning("当前无人机未连接");
            return;
        }
        if (!isFeatureSupported(ADJUST_PITCH)) {
            ToastUtil.showWarning("当前设备不支持云台控制");
            return;
        }
        float value = getGradientVertical();
        if (value <= 0) {
            ToastUtil.showWarning("当前设备未连接或不支持该操作");
            return;
        }
        controlUpAndDown(value);
    }

    public void gimDown() {
        if (!isFeatureSupported(ADJUST_PITCH)) {
            ToastUtil.showWarning("当前设备不支持云台控制");
            return;
        }
        float value = getGradientVertical();
        if (value <= 0) {
            ToastUtil.showWarning("当前设备未连接或不支持该操作");
            return;
        }

        controlUpAndDown(-value);
    }

    public void gimLeft() {
        if (!isFeatureSupported(ADJUST_YAW)) {
            LogUtils.e("设备不支持该操作");
            return;
        }
        float value = getGradientHorizon();
        if (value <= 0) {
            LogUtils.e("设备不支持该操作");
            ToastUtil.showWarning("当前设备未连接或不支持该操作");
            return;
        }
        LogUtils.i("当前梯度：" + -value);
        controlLeftAndRight(-value);
    }

    public void gimRight() {
        if (!isFeatureSupported(ADJUST_YAW)) {
            ToastUtil.showWarning("当前设备不支持云台控制");
            return;
        }
        float value = getGradientHorizon();
        if (value <= 0) {
            ToastUtil.showWarning("当前设备未连接或不支持该操作");
            return;
        }
        LogUtils.i("当前梯度：" + value);
        controlLeftAndRight(value);
    }

    public void gimRoll() {
        float value = getGradientRoll();
        if (value <= 0) {
            showTipsNotSupport();
            return;
        }
        LogUtils.i("当前梯度：" + value);
        controlRoll(value);
    }

    private boolean isFeatureSupported(CapabilityKey key) {
        Gimbal gimbal = getGimInstance();
        if (gimbal == null) {
            return false;
        }
        DJIParamCapability capability = null;
        if (gimbal.getCapabilities() != null) {
            capability = gimbal.getCapabilities().get(key);
        }

        if (capability != null) {
            return capability.isSupported();
        }
        return false;
    }

    public void runUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    private void showTipsNotSupport() {
        runUiThread(() -> {
            ToastUtil.showWarning("当前机型不支持该指令");
        });
    }
}
