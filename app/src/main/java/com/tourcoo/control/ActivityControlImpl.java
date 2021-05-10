package com.tourcoo.control;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.fragment.app.FragmentManager;

import com.tourcoo.util.KeyboardHelper;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日16:35
 * @Email: 971613168@qq.com
 */
public class ActivityControlImpl implements ActivityFragmentControl, ActivityDispatchEventControl {
    @Override
    public boolean dispatchTouchEvent(Activity activity, MotionEvent event) {
        //根据事件派发全局控制点击非EditText 关闭软键盘
        if (activity != null) {
            KeyboardHelper.handleAutoCloseKeyboard(true, activity.getCurrentFocus(), event, activity);
        }
        return false;
    }

    @Override
    public boolean dispatchGenericMotionEvent(Activity activity, MotionEvent event) {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(Activity activity, KeyEvent event) {
        return false;
    }

    @Override
    public boolean dispatchKeyShortcutEvent(Activity activity, KeyEvent event) {
        return false;
    }

    @Override
    public boolean dispatchTrackballEvent(Activity activity, MotionEvent event) {
        return false;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(Activity activity, AccessibilityEvent event) {
        return false;
    }

    @Override
    public void setContentViewBackground(View contentView, Class<?> cls) {
//避免背景色重复
      /*  if (!Fragment.class.isAssignableFrom(cls)
                && contentView.getBackground() == null) {
            contentView.setBackgroundResource(R.color.colorBackground);
        } else {
            if (BaseActivity.class.isAssignableFrom(cls) || BaseFragment.class.isAssignableFrom(cls)) {
                return;
            }
            Activity activity = StackUtil.getInstance().getCurrent();
            if (activity.getClass().getSimpleName().equals("UniversalActivity")) {
                contentView.setBackgroundColor(Color.WHITE);
            }
            LogUtils.i("setContentViewBackground_activity:" + activity.getClass().getSimpleName() + ";cls:" + cls.getSimpleName());
        }*/
    }

    @Override
    public Application.ActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
        return new ActivityLifecycleCallbacksImpl() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                super.onActivityCreated(activity, savedInstanceState);
                //设置类全面屏手势滑动返回
                //todo
               /* if (!(activity instanceof FlyControlActivity)) {
                    SlideBack.with(activity)
                            .haveScroll(true)
                            .edgeMode(SlideBack.EDGE_BOTH)
                            .callBack(activity::onBackPressed)
                            .register();
                }*/
            }
        };
    }

    @Override
    public FragmentManager.FragmentLifecycleCallbacks getFragmentLifecycleCallbacks() {
        return new FragmentManager.FragmentLifecycleCallbacks() {
        };
    }


}
