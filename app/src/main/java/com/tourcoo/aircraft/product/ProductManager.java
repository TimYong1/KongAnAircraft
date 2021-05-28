package com.tourcoo.aircraft.product;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.ui.sample.AircraftApplication;
import com.tourcoo.aircraft.ui.sample.showcase.defaultlayout.TestActivity;
import com.tourcoo.entity.event.CommonEvent;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.util.StackUtil;
import com.tourcoo.util.StringUtil;
import com.tourcoo.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.product.Model;
import dji.sdk.accessory.AccessoryAggregation;
import dji.sdk.accessory.beacon.Beacon;
import dji.sdk.accessory.speaker.Speaker;
import dji.sdk.accessory.spotlight.Spotlight;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallSession;

import static com.tourcoo.constant.EventConstant.EVENT_AIRCRAFT_CONNECT;
import static com.tourcoo.constant.EventConstant.EVENT_AIRCRAFT_DISCONNECT;
import static com.tourcoo.constant.EventConstant.EVENT_PHONE_HANG_UP;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年04月30日13:43
 * @Email: 971613168@qq.com
 */
public class ProductManager {
    public static final String TAG = "ProductManager";
    private DJISDKManager.SDKManagerCallback registrationCallback;
    private final AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private boolean hasRegister;
    private static boolean isAppStarted = false;
    private IRongReceivedCallListener iRongReceivedCallListener;
    private String droneId;
    private ProductManager() {
        initListener();
    }

    private static BaseProduct product;

    private static class Holder {
        private final static ProductManager instance = new ProductManager();
    }

    public static ProductManager getInstance() {
        return Holder.instance;
    }


    /**
     * 注册
     */
    public void startSDKRegistration() {
        if (DJISDKManager.getInstance() == null) {
            LogUtils.e(TAG + "注册被拦截");
            return;
        }

        if (isRegistrationInProgress.compareAndSet(false, true)) {
            ThreadManager.getCache().execute(new Runnable() {
                @Override
                public void run() {
                    registrationCallback = new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            isRegistrationInProgress.set(false);
                            if (djiError == null) {
                                LogUtils.e(TAG + "激活受限");
                                return;
                            }
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                DJISDKManager.getInstance().startConnectionToProduct();
                                hasRegister = true;
                                LogUtils.i(TAG + "sdk注册成功");
                            } else {
                                LogUtils.e(TAG + "sdk注册失败:" + djiError);
                                hasRegister = false;
                            }
                        }

                        @Override
                        public void onProductDisconnect() {
                            LogUtils.d(TAG + "onProductDisconnect");
                            DJISDKManager.getInstance().startConnectionToProduct();
                            EventBus.getDefault().post(new CommonEvent(EVENT_AIRCRAFT_DISCONNECT));
                        }

                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            LogUtils.i(TAG + "设备已连接连接");
                            EventBus.getDefault().post(new CommonEvent(EVENT_AIRCRAFT_CONNECT));
                        }

                        @Override
                        public void onProductChanged(BaseProduct baseProduct) {

                        }

                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent baseComponent, BaseComponent baseComponent1) {

                        }

                        @Override
                        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                        }

                        @Override
                        public void onDatabaseDownloadProgress(long l, long l1) {

                        }
                    };
                    DJISDKManager.getInstance().registerApp(AircraftApplication.getContext(), registrationCallback);
                    LogUtils.i(TAG + "已执行注册");
                }
            });
        }
    }

    public void release() {
        registrationCallback = null;
        isRegistrationInProgress.set(false);
        product = null;
        DJISDKManager.getInstance().destroy();
        RongCallClient.getInstance().unregisterVideoFrameObserver();
        iRongReceivedCallListener = null;
        isAppStarted = false;
        RongCallClient.getInstance().setVoIPCallListener(null);
    }

    public boolean hasRegister() {
        return null != registrationCallback && hasRegister;
    }


    private void initListener() {
        isAppStarted = true;
        if (iRongReceivedCallListener == null) {
            iRongReceivedCallListener = new IRongReceivedCallListener() {
                /**
                 * 来电回调
                 * @param callSession 通话实体
                 */
                @Override
                public void onReceivedCall(RongCallSession callSession) {
                    LogUtils.d(TAG + "执行了2");
                    handleCallComing();
//                    loadUiState();
                }

                /**
                 * targetSDKVersion 大于等于 23 时检查权限的回调。当 targetSDKVersion 小于 23 的时候不需要实现。
                 * 在这个回调里用户需要使用Android6.0新增的动态权限分配接口requestCallPermissions通知用户授权，
                 * 然后在onRequestPermissionResult回调里根据用户授权或者不授权分别回调
                 * RongCallClient.getInstance().onPermissionGranted()和
                 * RongCallClient.getInstance().onPermissionDenied()来通知CallLib。
                 * @param callSession 通话实体
                 */
                @Override
                public void onCheckPermission(RongCallSession callSession) {
                    handleCallComing();
                    ToastUtil.showWarning("需要通话权限");
                }
            };
        }
        RongCallClient.setReceivedCallListener(iRongReceivedCallListener);
        LogUtils.d(TAG + "执行了1");
    }


    /**
     * 处理来电话的逻辑
     */
    private void handleCallComing() {
        if (StackUtil.getInstance().getActivity(TestActivity.class) != null) {
            EventBus.getDefault().post(new CommonEvent(EVENT_PHONE_HANG_UP));
            LogUtils.d(TAG + "管理后台发起了语音通话");
        } else {
            LogUtils.d(TAG + "需要挂断电话");
            phoneOff();
        }
      /*  if (vibratorPlayer == null) {
            vibratorPlayer = new VibratorPlayer(FlyApplication.getContext());
        }
        vibratorPlayer.play(50, 1500, false);*/

    }

    private void phoneOff() {
        // im未连接或者不在通话中，RongCallClient 和 RongCallSession 为空
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().hangUpCall(RongCallClient.getInstance().getCallSession().getCallId());
        }
    }


    /**
     * Gets instance of the specific product connected after the
     * API KEY is successfully validated. Please make sure the
     * API_KEY has been added in the Manifest
     */
    public static synchronized BaseProduct getProductInstance() {
        product = DJISDKManager.getInstance().getProduct();
        return product;
    }

    public  static  boolean isAppStarted() {
        return isAppStarted;
    }

    public String getDroneId() {
        return StringUtil.getNotNullValue(droneId);
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }
}
