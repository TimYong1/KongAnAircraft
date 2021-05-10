package com.tourcoo.control;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tourcoo.aircraft.ui.sample.AircraftApplication;
import com.tourcoo.dialog.loading.IosLoadingDialog;
import com.tourcoo.dialog.loading.LoadingDialogWrapper;

import static com.tourcoo.retrofit.RetrofitConstant.EXCEPTION_NOT_INIT_FAST_MANAGER;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月28日16:57
 * @Email: 971613168@qq.com
 */
public class UiManager {
    /**
     * 配置Activity/Fragment(背景+Activity强制横竖屏+Activity 生命周期回调+Fragment生命周期回调)
     */
    private ActivityFragmentControl mActivityFragmentControl;
    /**
     * 配置BasisActivity 子类事件派发相关
     */
    private ActivityDispatchEventControl mActivityDispatchEventControl;
    public static final String TAG = "UiManager";

    //原本在Provider中默认进行初始化,如果app出现多进程使用该模式可避免调用异常出现
    static {
        Application application = AircraftApplication.getContext();
        if (application != null) {
            Log.i(TAG, "initSuccess");
            init(application);
        }
    }

    private static volatile UiManager sInstance;

    private UiManager() {
    }

    public static UiManager getInstance() {
        if (sInstance == null) {
            throw new NullPointerException(EXCEPTION_NOT_INIT_FAST_MANAGER);
        }
        return sInstance;
    }

    private static Application mApplication;
    /**
     * Adapter加载更多View
     */

    /**
     * 配置全局通用加载等待Loading提示框
     */
    private LoadingDialog mLoadingDialog;



    /*  *//**
     * 配置BasisActivity 子类前台时监听按键相关
     *//*
    private ActivityKeyEventControl mActivityKeyEventControl;*/

    /**
     * 配置网络请求
     */
    private HttpPageRequestControl mHttpPageRequestControl;

    private HttpRequestControl mHttpRequestControl;

    /**
     *
     */
    private ObserverControl mObserverControl;


    public Application getApplication() {
        return mApplication;
    }

    /**
     * 不允许外部调用
     *
     * @param application Application 对象
     * @return
     */
    static UiManager init(Application application) {
        Log.i("FastManager", "init_mApplication:" + mApplication + ";application;" + application);
        //保证只执行一次初始化属性
        if (mApplication == null && application != null) {
            mApplication = application;
            sInstance = new UiManager();
            //预设置FastLoadDialog属性
            sInstance.setLoadingDialog(new LoadingDialog() {
                @Nullable
                @Override
                public LoadingDialogWrapper createLoadingDialog(@Nullable Activity activity) {
                    return new LoadingDialogWrapper(activity, new IosLoadingDialog(activity));
                }
            });
//            初始化Toast工具
            //注册activity生命周期
            mApplication.registerActivityLifecycleCallbacks(new ApplicationLifecycleCallbacks());
        }
        return getInstance();
    }


    public LoadingDialog getLoadingDialog() {
        return mLoadingDialog;
    }

    /**
     * 设置全局网络请求等待Loading提示框如登录等待loading
     *
     * @param control
     * @return
     */
    public UiManager setLoadingDialog(LoadingDialog control) {
        if (control != null) {
            this.mLoadingDialog = control;
        }
        return this;
    }


    public HttpPageRequestControl getHttpRequestPageControl() {
        return mHttpPageRequestControl;
    }

    public HttpRequestControl getRequestControl() {
        return mHttpRequestControl;
    }

    /**
     * 配置Http请求成功及失败相关回调-方便全局处理
     *
     * @param control
     * @return
     */
    public UiManager setHttpPageRequestControl(HttpPageRequestControl control) {
        mHttpPageRequestControl = control;
        return this;
    }

    public UiManager setHttpRequestControl(HttpRequestControl control) {
        mHttpRequestControl = control;
        return this;
    }

    public ObserverControl getObserverControl() {
        return mObserverControl;
    }

    /**
     * @param control ObserverControl对象
     * @return
     */
    public UiManager setObserverControl(ObserverControl control) {
        mObserverControl = control;
        return this;
    }


    public ActivityFragmentControl getActivityFragmentControl() {
        return mActivityFragmentControl;
    }

    /**
     * 配置Activity/Fragment(背景+Activity强制横竖屏+Activity 生命周期回调+Fragment生命周期回调)
     *
     * @param control
     * @return
     */
    public UiManager setActivityFragmentControl(ActivityFragmentControl control) {
        mActivityFragmentControl = control;
        return this;
    }


    public ActivityDispatchEventControl getActivityDispatchEventControl() {
        return mActivityDispatchEventControl;
    }

    public UiManager setActivityDispatchEventControl(ActivityDispatchEventControl activityDispatchEventControl) {
        this.mActivityDispatchEventControl = activityDispatchEventControl;
        return this;
    }
}
