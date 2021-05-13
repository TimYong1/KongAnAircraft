package com.tourcoo.aircraft.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.aircraft.ui.account.UserInfoActivity;
import com.tourcoo.aircraft.ui.photo.FlyPhotoActivity;
import com.tourcoo.aircraft.ui.sample.showcase.defaultlayout.DefaultLayoutActivity;
import com.tourcoo.aircraft.ui.sample.showcase.defaultlayout.TestActivity;
import com.tourcoo.aircraftmanager.R;

import com.tourcoo.entity.BaseResult;
import com.tourcoo.entity.event.AirCraftEvent;
import com.tourcoo.entity.event.CommonEvent;
import com.tourcoo.entity.event.RegisterEvent;
import com.tourcoo.entity.sn.DeviceInfo;
import com.tourcoo.manager.AircraftHelper;
import com.tourcoo.retrofit.BaseLoadingObserver;
import com.tourcoo.retrofit.RequestConfig;
import com.tourcoo.retrofit.repository.ApiRepository;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.util.SpUtil;
import com.tourcoo.util.StringUtil;
import com.tourcoo.util.ToastUtil;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dji.common.error.DJIError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import io.rong.imlib.RongIMClient;

import static com.tourcoo.constant.AccountConstant.PREF_KEY_IS_LOGIN_DJ_ACCOUNT;
import static com.tourcoo.constant.ActionConstant.ACTION_AIR_CRAFT_DIS_CONNECT;
import static com.tourcoo.constant.ActionConstant.ACTION_AIR_CRAFT_UPLOAD_INFO;
import static com.tourcoo.constant.EventConstant.EVENT_AIRCRAFT_CONNECT;
import static com.tourcoo.constant.EventConstant.EVENT_AIRCRAFT_DISCONNECT;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年04月19日18:06
 * @Email: 971613168@qq.com
 */
public class HomeActivity extends RxAppCompatActivity implements View.OnClickListener {
    public static final String TAG = "HomeActivity";
    private Activity mContext;
    private TextView tvConnectStatus, tvAirCraftName;
    private static boolean isAppStarted = false;
    private ImageView ivConnectStatus, ivHandFly, ivFlyMap, ivFlyPhotoAlbum;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE, // Gimbal rotation
            Manifest.permission.INTERNET, // API requests
            Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH, // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO, // Speaker accessory
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };
    private static final int REQUEST_PERMISSION_CODE = 1001;
    private List<String> missingPermission = new ArrayList<>();

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivHandFly:
                skipFlyControl();
                break;
            case R.id.ivFlyMap:
                ToastUtil.showNormal("敬请期待");
                break;
            case R.id.ivFlyPhotoAlbum:
                skipFlyPhoto();
                break;
            case R.id.llMy:
                skipUserInfo();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_home);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initViewAndClick();
        isAppStarted = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndRequestPermissions();
            }
        }, 5000);

    }

    private void getSnNumberAndUpload() {
        ThreadManager.getDefault().execute(new Runnable() {
            @Override
            public void run() {
                if (!true) {
                    return;
                }
                DeviceInfo deviceInfo = new DeviceInfo();
                Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
                deviceInfo.type = aircraft.getModel().getDisplayName();
                aircraft.getName(new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String s) {
                        System.out.println("getName  ：" + s);
                        deviceInfo.name = s;
                    }

                    @Override
                    public void onFailure(DJIError djiError) {

                    }
                });
                aircraft.getRemoteController().getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String s) {
//                System.out.println("遥控器序列号：" + s);
                        deviceInfo.remoteSn = s;
                        deviceInfo.id = s;
                    }

                    @Override
                    public void onFailure(DJIError djiError) {

                    }
                });
                aircraft.getFlightController().getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String s) {
                        System.out.println("飞控序列号：" + s);
                        deviceInfo.productSn = s;
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                    }
                });

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestHasUpload(deviceInfo);
                    }
                }, 200);
            }
        });


    }

    private void requestHasUpload(DeviceInfo deviceInfo) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("id", deviceInfo.id);
        params.put("name", deviceInfo.name);
        params.put("productSn", deviceInfo.productSn);
        params.put("remoteSn", deviceInfo.remoteSn);
        params.put("type", deviceInfo.type);
        ApiRepository.getInstance().requestUploadDroneData(params).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseLoadingObserver<BaseResult<Object>>() {
            @Override
            public void onRequestSuccess(BaseResult<Object> entity) {
                if (entity == null) {
                    return;
                }
                if (entity.status == RequestConfig.REQUEST_CODE_SUCCESS) {
                    AircraftHelper.getInstance().setUpload(true);
                    LogUtils.i(TAG + "上传成功");
                } else {
                    LogUtils.w(TAG + entity.status);
                }
            }

            @Override
            public void onRequestError(Throwable throwable) {
                super.onRequestError(throwable);
                LogUtils.tag(TAG).i(TAG + "onRequestError=" + throwable.toString());
            }
        });
    }

    private void hideNavigation() {
        /**
         * 隐藏虚拟按键，并且全屏
         */
        View decorView = HomeActivity.this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigation();
        setLandScape();
        boolean hasRegistered = DJISDKManager.getInstance().hasSDKRegistered();
        showUiByAircraftStatus();
        ToastUtil.showNormalDebug("是否注册：" + hasRegistered);
       /* if (!hasRegistered) {
            boolean success = DJISDKManager.getInstance().startConnectionToProduct();
            if (!success) {
                LogUtils.d(TAG + "是否连接成功=false");
                DJISDKManager.getInstance().stopConnectionToProduct();
                LogUtils.d(TAG + "准备再次连接");
                boolean success1 = DJISDKManager.getInstance().startConnectionToProduct();
                LogUtils.d(TAG + "再次连接的结果="+success1);
            }

        } else {
            LogUtils.e(TAG + "未注册");
        }*/
        doLoginDJAccount();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void setLandScape() {
        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            ProductManager.getInstance().startSDKRegistration();
        } else {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * 权限回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            ProductManager.getInstance().startSDKRegistration();
        } else {
            //todo
            Toast.makeText(getApplicationContext(), "缺少相关权限，注册受限", Toast.LENGTH_LONG).show();
        }
    }

    private void skipFlyControl() {
        Intent intent = new Intent();
        intent.setClass(mContext, TestActivity.class);
//        intent.setClass(mContext, DefaultLayoutActivity.class);
        startActivity(intent);
    }

    private void skipUserInfo() {
        Intent intent = new Intent();
        intent.setClass(mContext, UserInfoActivity.class);
        startActivity(intent);
    }


    private void skipFlyPhoto() {
        if(!AircraftUtil.isAircraftConnected()||(!AircraftUtil.isMediaManagerAvailable())){
            ToastUtil.showNormal("当前飞行器未连接");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(mContext, FlyPhotoActivity.class);
        startActivity(intent);
    }


    private void initViewAndClick() {
        ivConnectStatus = findViewById(R.id.ivConnectStatus);
        tvConnectStatus = findViewById(R.id.tvConnectStatus);
        ivHandFly = findViewById(R.id.ivHandFly);
        ivFlyMap = findViewById(R.id.ivFlyMap);
        ivFlyPhotoAlbum = findViewById(R.id.ivFlyPhotoAlbum);
        tvAirCraftName = findViewById(R.id.tvAirCraftName);
        ivHandFly.setOnClickListener(this);
        ivFlyMap.setOnClickListener(this);
        ivFlyPhotoAlbum.setOnClickListener(this);
        findViewById(R.id.llMy).setOnClickListener(this);
        findViewById(R.id.ivFlyMap).setOnClickListener(this);
    }


    private void showConnectSuccess(BaseProduct baseProduct) {
        if (baseProduct == null) {
            return;
        }
        runOnUiThread(() -> {
            tvConnectStatus.setText("已连接");
            if (AircraftUtil.isAircraft()) {
                if (baseProduct.getModel() != null) {
                    tvAirCraftName.setText(baseProduct.getModel().name());
                }
            }
            ivConnectStatus.setImageResource(R.drawable.shape_circle_green);
        });


    }

    private void showDisConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectStatus.setText("未连接");
                tvAirCraftName.setText("连接无人机");
                ivConnectStatus.setImageResource(R.drawable.shape_circle_red);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();

    }


    private void release() {
        try {
            if (DJISDKManager.getInstance() != null) {
                DJISDKManager.getInstance().stopConnectionToProduct();
            }
            if (RongIMClient.getInstance() != null) {
                RongIMClient.getInstance().disconnect();
            }
            ProductManager.getInstance().release();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, e.toString());
        }
        EventBus.getDefault().unregister(this);
    }

    private class AirCraftReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG + "接收到广播");
            if (intent == null) {
                return;
            }
            switch (StringUtil.getNotNullValue(intent.getAction())) {
                case ACTION_AIR_CRAFT_UPLOAD_INFO:
                    getSnNumberAndUpload();
                    break;
                case ACTION_AIR_CRAFT_DIS_CONNECT:
                    showDisConnect();
                    break;
                default:
                    break;
            }

        }
    }


    private void showUiByAircraftStatus() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CommonEvent event) {
        if(event == null){
            return;
        }
        switch (StringUtil.getNotNullValue(event.getAction())) {
            case EVENT_AIRCRAFT_CONNECT:
                if (ProductManager.getProductInstance() != null) {
                    showConnectSuccess(ProductManager.getProductInstance());
                } else {
                    showDisConnect();
                }
                break;
            case EVENT_AIRCRAFT_DISCONNECT:
                showDisConnect();
                break;
            default:
                break;
        }
       /* if (event != null) {
            BaseProduct baseProduct = event.GET();
            if (baseProduct != null) {
                showConnectSuccess(baseProduct);
            } else {
                showDisConnect();
            }
            switch (event.getCode()) {
                case 100:
                    doLoginDJAccount();
                    break;
                default:
                    break;
            }
        }*/
        /* Do something */
    }

    private void doLoginDJAccount() {
        Boolean isLoginDj = SpUtil.INSTANCE.getBoolean(PREF_KEY_IS_LOGIN_DJ_ACCOUNT);
        if (isLoginDj != null && !isLoginDj) {
            loginAccount();
        }

    }

    private void loginAccount() {
        if (!DJISDKManager.getInstance().hasSDKRegistered()) {
            return;
        }
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        SpUtil.INSTANCE.put(PREF_KEY_IS_LOGIN_DJ_ACCOUNT, true);
                        ToastUtil.showSuccess("飞行限制已解除");
                    }

                    @Override
                    public void onFailure(DJIError error) {
                        if (error != null) {
                            ToastUtil.showWarningCondition(error.toString(),"登录已取消");
                        }

                    }
                });
    }

    public static boolean isStarted() {
        return isAppStarted;
    }

}
