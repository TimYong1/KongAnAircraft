package com.tourcoo.aircraft.ui.sample.showcase.defaultlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.apkfuns.logutils.LogUtils;
import com.dji.mapkit.core.maps.DJIMap;
import com.dji.mapkit.core.models.DJILatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tourcoo.account.AccountHelper;
import com.tourcoo.aircraft.FlightRealDataManager;
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.aircraft.ui.sample.AircraftApplication;
import com.tourcoo.aircraft.widget.camera.CameraHelper;
import com.tourcoo.aircraft.widget.gimble.GimHelper;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.entity.BaseResult;
import com.tourcoo.entity.event.CommonEvent;
import com.tourcoo.entity.flight.FlightRealTimeData;
import com.tourcoo.entity.flight.LocateData;
import com.tourcoo.entity.socket.BaseSocketResult;
import com.tourcoo.live.LiveStreamHelper;
import com.tourcoo.retrofit.BaseLoadingObserver;
import com.tourcoo.retrofit.RequestConfig;
import com.tourcoo.retrofit.repository.ApiRepository;
import com.tourcoo.socket.WebSocketCallBack;
import com.tourcoo.socket.WebSocketManager;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.timer.OnCountDownTimerListener;
import com.tourcoo.timer.TimeTool;
import com.tourcoo.util.LocateHelper;
import com.tourcoo.util.SpUtil;
import com.tourcoo.util.ToastUtil;
import com.tourcoo.util.VibratorPlayer;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import dji.common.airlink.PhysicalSource;
import dji.common.error.DJIError;
import dji.keysdk.KeyManager;
import dji.thirdparty.io.reactivex.android.schedulers.AndroidSchedulers;
import dji.thirdparty.io.reactivex.disposables.CompositeDisposable;
import dji.ux.beta.accessory.widget.rtk.RTKWidget;
import dji.ux.beta.cameracore.widget.fpvinteraction.FPVInteractionWidget;
import dji.ux.beta.core.extension.ViewExtensions;
import dji.ux.beta.core.panel.systemstatus.SystemStatusListPanelWidget;
import dji.ux.beta.core.util.SettingDefinitions;
import dji.ux.beta.core.widget.fpv.FPVWidget;
import dji.ux.beta.map.widget.map.MapWidget;
import dji.ux.beta.training.widget.simulatorcontrol.SimulatorControlWidget;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;

import static com.tourcoo.constant.CommandConstant.COMMAND_CAMERA_MODE;
import static com.tourcoo.constant.CommandConstant.COMMAND_RECORD_MODE;
import static com.tourcoo.constant.CommandConstant.COMMAND_START_RECORD;
import static com.tourcoo.constant.CommandConstant.COMMAND_STOP_RECORD;
import static com.tourcoo.constant.CommandConstant.COMMAND_TAKE_PHOTO;
import static com.tourcoo.constant.CommandConstant.COMMAND_WEB_YUN_TAI_CENTER;
import static com.tourcoo.constant.CommandConstant.COMMAND_WEB_YUN_TAI_DOWN;
import static com.tourcoo.constant.CommandConstant.COMMAND_WEB_YUN_TAI_LEFT;
import static com.tourcoo.constant.CommandConstant.COMMAND_WEB_YUN_TAI_RIGHT;
import static com.tourcoo.constant.CommandConstant.COMMAND_WEB_YUN_TAI_UP;
import static com.tourcoo.constant.EventConstant.EVENT_PHONE_HANG_UP;
import static com.tourcoo.constant.LocateConstant.PREF_KEY_LAST_LOCATE_LANG;
import static com.tourcoo.constant.LocateConstant.PREF_KEY_LAST_LOCATE_LAT;
import static com.tourcoo.entity.socket.SocketConstant.SOCKET_TYPE_REAL_TIME_DATA_FLIGHT;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年04月29日15:39
 * @Email: 971613168@qq.com
 */
public class TestActivity extends RxAppCompatActivity implements View.OnClickListener {
    public static final String TAG = "TestActivity";
    private ConstraintLayout rootView;
    private FPVInteractionWidget fpvInteractionWidget;
    private MapWidget mapWidget;
    //主镜头视频组件
    private FPVWidget fpvWidgetPrimary;
    //副镜头视频组件
    private FPVWidget fpvWidgetSecond;
    private RTKWidget rtkWidget;
    private boolean isMapMini = true;
    private int widgetHeight;
    private int widgetWidth;
    private int widgetMargin;
    private int deviceWidth;
    private int deviceHeight;
    private CompositeDisposable compositeDisposable;
    private SystemStatusListPanelWidget systemStatusListPanelWidget;
    private SimulatorControlWidget simulatorControlWidget;
    public WebSocketManager webSocketManager;
    private TimeTool timer;
    private static final int timeInterval = 2000;
    private Gson gson;
    private BaseSocketResult socketUploadEntity;
    private int phoneState = 0;
    private LocateData userLocate;
    private static final int PHONE_NO_CALL = 0;
    /**
     * 响铃中
     */
    private static final int PHONE_RING_UP = 1;
    /**
     * 正在通话
     */
    private static final int PHONE_ON = 2;
    /**
     * 电话已挂断
     */
    private static final int PHONE_OFF = -1;
    private VibratorPlayer vibratorPlayer = null;

    private ImageView ivLive, ivCall, ivCallClose;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_new);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initView();
        findViewById(R.id.icBackHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mapWidget = findViewById(R.id.widget_map);
        mapWidget.initAMap(map -> {
            map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                @Override
                public void onMapClick(DJILatLng djiLatLng) {
                    handleMapClick(mapWidget);
                }
            });
            map.getUiSettings().setZoomControlsEnabled(false);
        });
        mapWidget.onCreate(savedInstanceState);
        initData();

    }


    private void initView() {
        rootView = findViewById(R.id.rootView);
        findViewById(R.id.statusView).setOnClickListener(this);
        fpvInteractionWidget = findViewById(R.id.widget_fpv_interaction);
        mapWidget = findViewById(R.id.widget_map);
        fpvWidgetPrimary = findViewById(R.id.widget_fpv);
        ivLive = findViewById(R.id.ivLive);
        ivCall = findViewById(R.id.ivCall);
        ivCallClose = findViewById(R.id.ivCallClose);
        ivLive.setOnClickListener(this);
        ivCall.setOnClickListener(this);
        ivCallClose.setOnClickListener(this);
        fpvWidgetSecond = findViewById(R.id.widget_secondary_fpv);
        systemStatusListPanelWidget = findViewById(R.id.widget_panel_system_status_list);
        simulatorControlWidget = findViewById(R.id.widget_simulator_control);
        fpvWidgetPrimary.setOnClickListener(this);
        rtkWidget = findViewById(R.id.widget_rtk);
        widgetHeight = (int) getResources().getDimension(R.dimen.mini_map_height);
        widgetWidth = (int) getResources().getDimension(R.dimen.mini_map_width);
        widgetMargin = (int) getResources().getDimension(R.dimen.mini_map_margin);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
    }

    private void handleMapClick(View view) {
        if (view == fpvWidgetPrimary && !isMapMini) {
            //reorder widgets
            rootView.removeView(fpvWidgetPrimary);
            rootView.addView(fpvWidgetPrimary, 0);

            //resize widgets
            resizeViews(fpvWidgetPrimary, mapWidget);
            //enable interaction on FPV
            fpvInteractionWidget.setInteractionEnabled(true);
            //disable user login widget on map
//            userAccountLoginWidget.setVisibility(View.GONE);
            isMapMini = true;
        } else if (view == mapWidget && isMapMini) {
            //reorder widgets
            rootView.removeView(fpvWidgetPrimary);
            rootView.addView(fpvWidgetPrimary, rootView.indexOfChild(mapWidget) + 1);
            //resize widgets
            resizeViews(mapWidget, fpvWidgetPrimary);
            //disable interaction on FPV
            fpvInteractionWidget.setInteractionEnabled(false);
            //enable user login widget on map
//            userAccountLoginWidget.setVisibility(View.VISIBLE);
            isMapMini = false;
        }
    }

    /**
     * Helper method to resize the FPV and Map Widgets.
     *
     * @param viewToEnlarge The view that needs to be enlarged to full screen.
     * @param viewToShrink  The view that needs to be shrunk to a thumbnail.
     */
    private void resizeViews(View viewToEnlarge, View viewToShrink) {
        //enlarge first widget
        ResizeAnimation enlargeAnimation = new ResizeAnimation(viewToEnlarge, widgetWidth, widgetHeight, deviceWidth, deviceHeight, 0);
        viewToEnlarge.startAnimation(enlargeAnimation);

        //shrink second widget
        ResizeAnimation shrinkAnimation = new ResizeAnimation(viewToShrink, deviceWidth, deviceHeight, widgetWidth, widgetHeight, widgetMargin);
        viewToShrink.startAnimation(shrinkAnimation);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.statusView:
                ViewExtensions.show(systemStatusListPanelWidget);
                break;
            case R.id.widget_fpv:
                handleMapClick(v);
                break;
            case R.id.widget_secondary_fpv:
                swapVideoSource();
                break;
            case R.id.ivLive:
                doLiveShow();
                break;
            case R.id.ivCallClose:
                phoneOff();
                break;
            case R.id.ivCall:
                phoneOn();
                break;
            default:
                break;
        }
    }

    /**
     * Animation to change the size of a view.
     */
    private static class ResizeAnimation extends Animation {

        private static final int DURATION = 300;

        private View view;
        private int toHeight;
        private int fromHeight;
        private int toWidth;
        private int fromWidth;
        private int margin;

        private ResizeAnimation(View v, int fromWidth, int fromHeight, int toWidth, int toHeight, int margin) {
            this.toHeight = toHeight;
            this.toWidth = toWidth;
            this.fromHeight = fromHeight;
            this.fromWidth = fromWidth;
            view = v;
            this.margin = margin;
            setDuration(DURATION);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (toHeight - fromHeight) * interpolatedTime + fromHeight;
            float width = (toWidth - fromWidth) * interpolatedTime + fromWidth;
            ConstraintLayout.LayoutParams p = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            p.rightMargin = margin;
            p.bottomMargin = margin;
            view.requestLayout();
        }
    }
    //endregion


    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        EventBus.getDefault().unregister(this);
        release();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUiState();
        mapWidget.onResume();
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(fpvWidgetSecond.getCameraName()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateSecondaryVideoVisibility));

        compositeDisposable.add(systemStatusListPanelWidget.closeButtonPressed()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pressed -> {
                    if (pressed) {
                        ViewExtensions.hide(systemStatusListPanelWidget);
                    }
                }));

        compositeDisposable.add(rtkWidget.getUIStateUpdates()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uiState -> {
                    if (uiState instanceof RTKWidget.UIState.VisibilityUpdated) {
                        if (((RTKWidget.UIState.VisibilityUpdated) uiState).isVisible()) {
                            hideOtherPanels(rtkWidget);
                        }
                    }
                }));
        compositeDisposable.add(simulatorControlWidget.getUIStateUpdates()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(simulatorControlWidgetState -> {
                    if (simulatorControlWidgetState instanceof SimulatorControlWidget.UIState.VisibilityUpdated) {
                        if (((SimulatorControlWidget.UIState.VisibilityUpdated) simulatorControlWidgetState).isVisible()) {
                            hideOtherPanels(simulatorControlWidget);
                        }
                    }
                }));
    }

    @Override
    protected void onPause() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
        mapWidget.onPause();
        super.onPause();
    }
    //endregion

    //region Utils

    private void hideOtherPanels(@Nullable View widget) {
        View[] panels = {
                rtkWidget,
                simulatorControlWidget
        };

        for (View panel : panels) {
            if (widget != panel) {
                panel.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 切换FPV和辅助FPV小部件的视频源。
     */
    private void swapVideoSource() {
        if (fpvWidgetSecond.getVideoSource() == SettingDefinitions.VideoSource.SECONDARY) {
            fpvWidgetPrimary.setVideoSource(SettingDefinitions.VideoSource.SECONDARY);
            fpvWidgetSecond.setVideoSource(SettingDefinitions.VideoSource.PRIMARY);
        } else {
            fpvWidgetPrimary.setVideoSource(SettingDefinitions.VideoSource.PRIMARY);
            fpvWidgetSecond.setVideoSource(SettingDefinitions.VideoSource.SECONDARY);
        }
    }

    /**
     * Hide the secondary FPV widget when there is no secondary camera.
     *
     * @param cameraName The name of the secondary camera.
     */
    private void updateSecondaryVideoVisibility(String cameraName) {
        if (cameraName.equals(PhysicalSource.UNKNOWN.name())) {
            fpvWidgetSecond.setVisibility(View.GONE);
        } else {
            fpvWidgetSecond.setVisibility(View.VISIBLE);
        }
    }


    protected void setViewVisible(View view, boolean visible) {
        if (view == null) {
            return;
        }
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }


    /**
     * 处理来电话的逻辑
     */
    private void handleCallComing() {
        phoneState = PHONE_RING_UP;
        if (vibratorPlayer == null) {
            vibratorPlayer = new VibratorPlayer(AircraftApplication.getContext());
        }
        vibratorPlayer.play(50, 1500, false);
        ToastUtil.showNormal("管理后台发起了语音通话");
        loadUiState();
    }


    private void handleUploadStream(String url) {
        LiveStreamHelper.getInstance().startLiveShow(url);
        loadUiState();
//        ivLive.setImageResource(R.drawable.ic_live_stop);
        doHideNavigation();
    }

    private void doLiveShow() {
        if (LiveStreamHelper.getInstance().isLiveShowOpen()) {
            //停止直播
            LiveStreamHelper.getInstance().stopLiveShow();
            loadUiState();
        } else {
            if (!AircraftUtil.isAircraftConnected()) {
                ToastUtil.showWarning("当前无人机未连接或状态异常");
                return;
            }
            requestStreamUrlAndUpload();
        }
    }


    private void loadUiState() {
        if (LiveStreamHelper.getInstance().isLiveShowOpen()) {
            ivLive.setImageResource(R.drawable.ic_live_stop);
        } else {
            LogUtils.w("loadUiState");
            ivLive.setImageResource(R.drawable.ic_live_start);
        }
        switch (phoneState) {
            case PHONE_ON:
                setViewVisible(ivCall, false);
                setViewVisible(ivCallClose, true);
                break;
            case PHONE_NO_CALL:
            case PHONE_OFF:
                setViewVisible(ivCall, false);
                setViewVisible(ivCallClose, false);
                break;
            case PHONE_RING_UP:
                setViewVisible(ivCall, true);
                setViewVisible(ivCallClose, true);
                break;
            default:
                break;
        }

    }

    private void doHideNavigation() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideNavigation();
            }
        }, 50);
    }


    private void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (PHONE_RING_UP == phoneState ||PHONE_ON ==phoneState ) {
            phoneOff();
        }
        loadUiState();
        releaseSocketAndTimer();
        LocateHelper.getInstance().release();
        closeLiveShow();
        RongCallClient.getInstance().unregisterVideoFrameObserver();
        RongCallClient.getInstance().setVoIPCallListener(null);
    }

    private void requestStreamUrlAndUpload() {
        ApiRepository.getInstance().requestStreamUrl().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseLoadingObserver<BaseResult<String>>("正在获取直播地址...") {
            @Override
            public void onRequestSuccess(BaseResult<String> entity) {
                if (entity == null) {
                    ToastUtil.showSuccess("直播流地址获取失败");
                    return;
                }
                if (RequestConfig.RESPONSE_CODE_SUCCESS == entity.status && null != entity.data) {
                    handleUploadStream(entity.data);
                } else {
                    ToastUtil.showNormal(entity.message);
                }

            }

            @Override
            public void onRequestError(Throwable throwable) {
                super.onRequestError(throwable);
                ToastUtil.showNormalCondition(throwable.toString(), "直播流上传地址获取失败 无法推流");
            }
        });
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


    private void phoneOn() {
        // im未连接或者不在通话中，RongCallClient 和 RongCallSession 为空
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().acceptCall(RongCallClient.getInstance().getCallSession().getCallId());
            RongCallClient.getInstance().setVoIPCallListener(new IRongCallListener() {
                @Override
                public void onCallOutgoing(RongCallSession rongCallSession, SurfaceView surfaceView) {
                    phoneOff();
                    phoneState = PHONE_OFF;
                    loadUiState();
                }

                @Override
                public void onCallConnected(RongCallSession rongCallSession, SurfaceView surfaceView) {
                    //通话开启
                    phoneState = PHONE_ON;
                    //免提
                    RongCallClient.getInstance().setEnableSpeakerphone(true);
                    loadUiState();
                }

                @Override
                public void onCallDisconnected(RongCallSession rongCallSession, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {
                    phoneState = PHONE_OFF;
                    loadUiState();
                }

                @Override
                public void onRemoteUserRinging(String s) {

                }

                @Override
                public void onRemoteUserJoined(String s, RongCallCommon.CallMediaType callMediaType, int i, SurfaceView surfaceView) {

                }

                @Override
                public void onRemoteUserInvited(String s, RongCallCommon.CallMediaType callMediaType) {

                }

                @Override
                public void onRemoteUserLeft(String s, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {

                }

                @Override
                public void onMediaTypeChanged(String s, RongCallCommon.CallMediaType callMediaType, SurfaceView surfaceView) {

                }

                @Override
                public void onError(RongCallCommon.CallErrorCode callErrorCode) {
                    ToastUtil.showNormalCondition(callErrorCode.toString(),"语音通话服务出了点小差");
                }

                @Override
                public void onRemoteCameraDisabled(String s, boolean b) {

                }

                @Override
                public void onRemoteMicrophoneDisabled(String s, boolean b) {

                }

                @Override
                public void onNetworkReceiveLost(String s, int i) {

                }

                @Override
                public void onNetworkSendLost(int i, int i1) {

                }

                @Override
                public void onFirstRemoteVideoFrame(String s, int i, int i1) {

                }

                @Override
                public void onAudioLevelSend(String s) {

                }

                @Override
                public void onAudioLevelReceive(HashMap<String, String> hashMap) {

                }

                @Override
                public void onRemoteUserPublishVideoStream(String s, String s1, String s2, SurfaceView surfaceView) {

                }

                @Override
                public void onRemoteUserUnpublishVideoStream(String s, String s1, String s2) {

                }
            });
            phoneState = PHONE_ON;
            loadUiState();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(CommonEvent event) {
        if (event == null) {
            return;
        }
        switch (event.getAction()) {
            case EVENT_PHONE_HANG_UP:
                handleCallComing();
                break;
            default:
                break;
        }

    }

    private void phoneOff() {
        // im未连接或者不在通话中，RongCallClient 和 RongCallSession 为空
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().hangUpCall(RongCallClient.getInstance().getCallSession().getCallId());
        }
        phoneState = PHONE_OFF;
        loadUiState();
    }


    @SuppressWarnings("unchecked")
    private void loadFlightRealData() {
        if (KeyManager.getInstance() == null) {
            return;
        }
        if (socketUploadEntity == null) {
            socketUploadEntity = new BaseSocketResult();
        }
        FlightRealTimeData flightRealTimeData = FlightRealDataManager.getInstance().getRealTimeData();
        flightRealTimeData.setUserLocateData(userLocate);
        socketUploadEntity.setData(flightRealTimeData);
        socketUploadEntity.setMsgType(SOCKET_TYPE_REAL_TIME_DATA_FLIGHT);
        String result = gson.toJson(socketUploadEntity);
        LogUtils.i(TAG + result);
        webSocketManager.send(result);

    }


    private void initSocket() {
        webSocketManager.setSocketIOCallBack(new WebSocketCallBack() {
            @Override
            public void onOpen() {
                LogUtils.i("WebSocket : 已成功连接" + webSocketManager.getStatus());

            }

            @Override
            public void onMessage(String text) {
                LogUtils.i("WebSocket : onMessage" + text);
                handleCommand(text);
            }

            @Override
            public void onClosed() {
                LogUtils.w("WebSocket : onClosed");
            }

            @Override
            public void onConnectError(Throwable throwable) {
                LogUtils.e("WebSocket : onConnectError" + throwable.toString());
            }
        });
        webSocketManager.connect();
    }


    private void initTimer() {
        if (timer == null) {
            timer = new TimeTool(Integer.MAX_VALUE, timeInterval);
        } else {
            timer.reset();
        }
        timer.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                loadFlightRealData();
            }

            @Override
            public void onFinish() {
                timer.reset();
                timer.start();
            }

            @Override
            public void onCancel() {
                LogUtils.d("");
            }
        });
        timer.start();
    }

    private void releaseTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }


    private void handleCommand(String command) {
        if (TextUtils.isEmpty(command) || command.equalsIgnoreCase("pong")) {
            return;
        }
        BaseSocketResult result = gson.fromJson(command, BaseSocketResult.class);
        if (result == null) {
            LogUtils.e(TAG + "后台指令异常");
            return;
        }
        switch (result.getMsgType()) {
            case COMMAND_CAMERA_MODE:
                CameraHelper.getInstance().setCameraModePhotoSingle(djiError -> {
                    LogUtils.d(TAG + "执行了1=" + djiError);
                    doReplyRequest(djiError, result);
                });
                break;
            case COMMAND_RECORD_MODE:
                CameraHelper.getInstance().setCameraModeRecord(djiError -> {
                    LogUtils.d(TAG + "执行了2=" + djiError);
                    doReplyRequest(djiError, result);
                });
                break;
            case COMMAND_START_RECORD:
                CameraHelper.getInstance().startRecord(djiError -> {
                    LogUtils.d(TAG + "执行了3=" + djiError);
                    doReplyRequest(djiError, result);
                });
                break;
            case COMMAND_STOP_RECORD:
                CameraHelper.getInstance().stopRecord(djiError -> {
                    doReplyRequest(djiError, result);
                });
                break;
            case COMMAND_TAKE_PHOTO:
                CameraHelper.getInstance().takePhoto(djiError -> {
                    doReplyRequest(djiError, result);
                });
                break;
            case COMMAND_WEB_YUN_TAI_UP:
                GimHelper.getInstance().gimUp();
                break;
            case COMMAND_WEB_YUN_TAI_DOWN:
                GimHelper.getInstance().gimDown();
                break;

            case COMMAND_WEB_YUN_TAI_LEFT:
                GimHelper.getInstance().gimLeft();
                break;
            case COMMAND_WEB_YUN_TAI_RIGHT:
                GimHelper.getInstance().gimRight();
                break;
            case COMMAND_WEB_YUN_TAI_CENTER:
                GimHelper.getInstance().gimRoll();
                break;
            default:
                break;
        }
    }

    private void doReplyRequest(DJIError djiError, BaseSocketResult result) {
        if (djiError == null) {
            reply(result, true);
        } else {
            reply(result, false);
        }
    }


    private void reply(BaseSocketResult result, boolean success) {
        if (result != null) {
            result.setData(success);
            webSocketManager.send(gson.toJson(result));
        }
    }

    private void releaseSocketAndTimer() {
        if (webSocketManager != null) {
            webSocketManager.release();
        }
        releaseTimer();
    }

    private void loadSocketAndTimer() {
        ThreadManager.getDefault().execute(new Runnable() {
            @Override
            public void run() {
                String socketUrl = AccountHelper.getInstance().getSocketUrl();
                LogUtils.d(TAG + "socket即将连接到" + socketUrl);
                webSocketManager = WebSocketManager.getInstance(socketUrl);
                initSocket();
                initTimer();
            }
        });

    }

    private void initData() {
        gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        loadSocketAndTimer();
        getCurrentLocateAndSetMap();
    }

    private void getCurrentLocateAndSetMap() {
        ThreadManager.getDefault().execute(new Runnable() {
            @Override
            public void run() {
                Double lastLat = SpUtil.INSTANCE.getDouble(PREF_KEY_LAST_LOCATE_LAT);
                Double lastLang = SpUtil.INSTANCE.getDouble(PREF_KEY_LAST_LOCATE_LANG);
                if (lastLat != null && lastLang != null) {
                    LatLng latLng = new LatLng(lastLat, lastLang);
                    //如果存在上次定位记录 则先用上次位置
                    setCurrentLatLngOnMap(latLng);
                }
                LocateHelper.getInstance().startLocate(getApplicationContext(), new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation aMapLocation) {
                        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                            LogUtils.i(TAG + "定位成功");
                            double lat = aMapLocation.getLatitude();
                            double lang = aMapLocation.getLongitude();
                            LatLng latLng = new LatLng(lat, lang);
                            setCurrentLatLngOnMap(latLng);
                            saveCurrentLatLangToLocal(lat, lang);
                            if (userLocate == null) {
                                userLocate = new LocateData();
                            }
                            userLocate.setLongitude(lang);
                            userLocate.setLatitude(lat);
                        } else {
                            LogUtils.e(TAG + "定位失败");
                        }
                    }
                });
            }
        });

    }


    private void setCurrentLatLngOnMap(LatLng currentLatLang) {
        DJIMap map = mapWidget.getMap();
        AMap aMap = (AMap) map.getMap();
        float level = aMap.getMaxZoomLevel();
        if (level > 3) {
            level = level - 3;
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLang, level));
        showBluePoint(aMap);

    }


    private void saveCurrentLatLangToLocal(double lat, double lang) {
        SpUtil.INSTANCE.put(PREF_KEY_LAST_LOCATE_LAT, lat);
        SpUtil.INSTANCE.put(PREF_KEY_LAST_LOCATE_LANG, lang);
    }


    private void showBluePoint(AMap aMap) {
        MyLocationStyle myLocationStyle;
        //初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        // （1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle = new MyLocationStyle();
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.interval(5000);
        //设置定位蓝点的icon图标方法，需要用到BitmapDescriptor类对象作为参数。
        BitmapDescriptorFactory.fromResource(R.mipmap.ic_locate_arrow);
        // 设置圆形的边框颜色
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        // 设置圆形的边框粗细
        myLocationStyle.strokeWidth(1.0f);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_locate_arrow));
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        //设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);
     /*   myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);//只定位一次。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW) ;//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）*/

//以下三种模式从5.1.0版本开始提供
     /*   myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，地图依照设备方向旋转，并且蓝点会跟随设备移动。*/
        aMap.setMyLocationStyle(myLocationStyle);
    }

    private void closeLiveShow() {
        if (LiveStreamHelper.getInstance().isLiveShowOpen()) {
            //停止直播
            LiveStreamHelper.getInstance().stopLiveShow();
            loadUiState();
        }
    }
}
