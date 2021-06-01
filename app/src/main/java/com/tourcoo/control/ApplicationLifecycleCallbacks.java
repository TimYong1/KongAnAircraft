package com.tourcoo.control;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.constant.CommonConstant;
import com.tourcoo.util.CommonUtil;
import com.tourcoo.util.StackUtil;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日9:59
 * @Email: 971613168@qq.com
 */
public class ApplicationLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private String TAG = getClass().getSimpleName();
    private ActivityFragmentControl mActivityFragmentControl;
    private FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        LogUtils.i(TAG+"onActivityCreated:" + activity.getClass().getSimpleName());
        getControl();
        //统一Activity堆栈管理
        StackUtil.getInstance().push(activity);
        //统一Fragment生命周期处理
        if (activity instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            fragmentManager.registerFragmentLifecycleCallbacks(this, true);
            if (mFragmentLifecycleCallbacks != null) {
                fragmentManager.registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks, true);
            }
        }

    }

    /**
     * 回调设置Activity/Fragment背景
     *
     * @param v
     * @param cls
     */
    private void setContentViewBackground(View v, Class<?> cls) {
        if (mActivityFragmentControl != null && v != null) {
            mActivityFragmentControl.setContentViewBackground(v, cls);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }


    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        LogUtils.i(TAG, "onActivityResumed:" + activity.getClass().getSimpleName());

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        LogUtils.i(TAG, "onActivityPaused:" + activity.getClass().getSimpleName() + ";isFinishing:" + activity.isFinishing());
        //Activity销毁前的时机需要关闭软键盘-在onActivityStopped及onActivityDestroyed生命周期内已无法关闭
        if (activity.isFinishing()) {
            //todo
//            KeyboardHelper.closeKeyboard(activity);
        }
        //回调给开发者实现自己应用逻辑

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        LogUtils.i(TAG, "onActivityStopped:" + activity.getClass().getSimpleName() + ";isFinishing:" + activity.isFinishing());
        //回调给开发者实现自己应用逻辑

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        LogUtils.i(TAG, "onActivitySaveInstanceState:" + activity.getClass().getSimpleName());
        //回调给开发者实现自己应用逻辑

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //横竖屏会重绘将状态重置
        if (activity.getIntent() != null) {
            activity.getIntent().removeExtra(CommonConstant.IS_SET_STATUS_VIEW_HELPER);
            activity.getIntent().removeExtra(CommonConstant.IS_SET_NAVIGATION_VIEW_HELPER);
            activity.getIntent().removeExtra(CommonConstant.IS_SET_CONTENT_VIEW_BACKGROUND);
            activity.getIntent().removeExtra(CommonConstant.IS_SET_REFRESH_VIEW);
            activity.getIntent().removeExtra(CommonConstant.IS_SET_TITLE_BAR_VIEW);
        }
        LogUtils.i(TAG, "onActivityDestroyed:" + activity.getClass().getSimpleName() + ";isFinishing:" + activity.isFinishing());
        StackUtil.getInstance().pop(activity, false);


        /*//清除BasisHelper
        DelegateManager.getInstance().removeBasisHelper(activity);*/
        //回调给开发者实现自己应用逻辑

    }

    @Override
    public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentViewDestroyed(fm, f);
        if (f.getArguments() != null) {
            f.getArguments().putBoolean(CommonConstant.IS_SET_CONTENT_VIEW_BACKGROUND, false);
        }

    }

    /**
     * 实时获取回调
     */

    private void getControl() {
        mActivityFragmentControl = UiManager.getInstance().getActivityFragmentControl();
        if (mActivityFragmentControl == null) {
            return;
        }
        mFragmentLifecycleCallbacks = mActivityFragmentControl.getFragmentLifecycleCallbacks();
    }


    /**
     * 获取Activity 顶部View(用于延伸至状态栏下边)
     *
     * @param target
     * @return
     */
    private View getTopView(View target) {
        if (target != null && target instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) target;
            if (group.getChildCount() > 0) {
                target = ((ViewGroup) target).getChildAt(0);
            }
        }
        return target;
    }


    @Override
    public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState);
        boolean isSet = f.getArguments() != null && f.getArguments().getBoolean(CommonConstant.IS_SET_CONTENT_VIEW_BACKGROUND, false);
        if (!isSet) {
            setContentViewBackground(v, f.getClass());
        }

    }
}


