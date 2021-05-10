
package com.tourcoo.aircraft.ui.sample;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.apkfuns.log2file.LogFileEngineFactory;
import com.apkfuns.logutils.LogUtils;
import com.secneo.sdk.Helper;
import com.tencent.mmkv.MMKV;
import com.tourcoo.account.AccountHelper;
import com.tourcoo.config.AppConfig;
import com.tourcoo.control.ActivityControlImpl;
import com.tourcoo.control.UiManager;
import com.tourcoo.control.impl.AppImpl;
import com.tourcoo.control.impl.HttpRequestControlImpl;
import com.tourcoo.retrofit.RequestConfig;
import com.tourcoo.retrofit.RetrofitHelper;
import com.tourcoo.rongyun.RYunManager;
import com.tourcoo.util.ToastUtil;

import dji.ux.beta.core.communication.DefaultGlobalPreferences;
import dji.ux.beta.core.communication.GlobalPreferencesManager;
import io.rong.imlib.RongIMClient;

import static com.tourcoo.aircraft.ui.sample.DJIConnectionControlActivity.ACCESSORY_ATTACHED;


public class AircraftApplication extends Application {
    private static Application app;
    public static final String TAG = "AircraftApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        initLog();
        ToastUtil.init(app);
        ActivityControlImpl activityControl = new ActivityControlImpl();
        AppImpl appImpl = new AppImpl();
        UiManager.getInstance().setObserverControl(appImpl).setHttpRequestControl(new HttpRequestControlImpl())
                //设置Adapter加载更多视图--默认设置了FastLoadMoreView
                .setActivityFragmentControl(activityControl).setActivityDispatchEventControl(activityControl);
        MMKV.initialize(this);
        //初始化Retrofit配置
        RetrofitHelper.getInstance()
                //配置全局网络请求BaseUrl
                .setBaseUrl(RequestConfig.BASE_URL)
                //信任所有证书--也可设置setCertificates(单/双向验证)
                .setCertificates()
                //设置统一请求头
//                .addHeader(header)
//                .addHeader(key,value)
                //设置请求全局log-可设置tag及Level类型
                .setLogEnable(true)
//                .setLogEnable(BuildConfig.DEBUG, TAG, HttpLoggingInterceptor.Level.BODY)
                //设置统一超时--也可单独调用read/write/connect超时(可以设置时间单位TimeUnit)
                //默认10 s
                .setTimeout(10);
        initRongYun();
        //要使全局首选项生效，必须在初始化小部件之前完成此操作
//如果未完成此操作，则不会在应用程序重新启动时生效或持久存在全局首选项
        GlobalPreferencesManager.initialize(new DefaultGlobalPreferences(this));
        BroadcastReceiver br = new OnDJIUSBAttachedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACCESSORY_ATTACHED);
        registerReceiver(br, filter);

    }

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(AircraftApplication.this);
        MultiDex.install(this);
        app = this;
    }

    public static Application getContext() {
        return app;
    }

    private void initLog() {
        // 设置日志写文件引擎
        LogUtils.getLog2FileConfig().configLogFileEngine(new LogFileEngineFactory(app));
        //不写入文件
        LogUtils.getLog2FileConfig().configLog2FileEnable(false);
        LogUtils.getLogConfig().configAllowLog(AppConfig.DEBUG_BODE).configShowBorders(false);
    }

    public static void initRongYun() {
        RongIMClient.init(app, "pwe86ga5psjb6", false);
        if (!AccountHelper.getInstance().isLogin()) {
            LogUtils.w("融云 当前未登录 无法连接融云服务器");
//            ToastUtil.showWarningDebug("当前未登录 无法连接融云服务器");
            return;
        }
        RongIMClient.connect(AccountHelper.getRyToken(), new RongIMClient.ConnectCallback() {
            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus code) {
                //消息数据库打开，可以进入到主页面
                LogUtils.i("融云" + "onDatabaseOpened=" + code);
            }

            @Override
            public void onSuccess(String s) {
                //连接成功
                LogUtils.i("融云连接成功" + "onSuccess=" + s);
                RYunManager.setConnected(true);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                Log.e("融云", "onError=" + errorCode);
                if (errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT)) {
                    //从 APP 服务获取新 token，并重连
                    ToastUtil.showWarningDebug("通话服务需要从 APP 服务获取新 token，并重连：" + errorCode);
                } else {
                    //无法连接 IM 服务器，请根据相应的错误码作出对应处理
                    ToastUtil.showFailedDebug("无法连接 IM 服务器，需要根据相应的错误码作出对应处理" + errorCode);
                }
            }
        });
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtils.e(TAG + "内存不足");
    }
}
