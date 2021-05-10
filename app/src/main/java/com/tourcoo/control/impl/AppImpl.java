package com.tourcoo.control.impl;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.tourcoo.control.LoadingDialog;
import com.tourcoo.control.ObserverControl;
import com.tourcoo.dialog.loading.IosLoadingDialog;
import com.tourcoo.dialog.loading.LoadingDialogWrapper;
import com.tourcoo.retrofit.BaseObserver;

import io.reactivex.Observable;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年03月18日19:18
 * @Email: 971613168@qq.com
 */
public class AppImpl implements LoadingDialog, ObserverControl {
    @Nullable
    @Override
    public LoadingDialogWrapper createLoadingDialog(@Nullable Activity activity) {
        return new LoadingDialogWrapper(activity, new IosLoadingDialog(activity,""));
    }


    /**
     * @param o {@link BaseObserver} 对象用于后续事件逻辑
     * @param e 原始错误
     * @return true 拦截操作不进行原始{@link BaseObserver#onError(Throwable)}后续逻辑
     * false 不拦截继续后续逻辑
     * {@link DataNullException} 已在{@link BaseObserver#onError} ｝处理如果为该类型Exception可不用管,参考
     * {@link BaseObserver#transform(Observable)} 处理逻辑
     */
    @Override
    public boolean onError(BaseObserver o, Throwable e) {
        return false;
    }
}
