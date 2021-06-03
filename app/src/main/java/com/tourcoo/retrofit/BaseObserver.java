package com.tourcoo.retrofit;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tourcoo.account.AccountHelper;
import com.tourcoo.control.IHttpPageRequestControl;
import com.tourcoo.control.IHttpRequestControl;
import com.tourcoo.control.UiManager;
import com.tourcoo.entity.AbstractResult;
import com.tourcoo.entity.BaseResultOld;

import java.io.IOException;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DefaultObserver;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

import static com.tourcoo.retrofit.RequestConfig.REQUEST_CODE_TOKEN_INVALID;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月27日17:59
 * @Email: 971613168@qq.com
 */
public abstract class BaseObserver<T> extends DefaultObserver<T> {
    protected IHttpPageRequestControl mHttpPageRequestControl;
    protected IHttpRequestControl requestControl;
    public static final String TAG = "BaseObserver";
    private Gson gson = new Gson();

    public BaseObserver(IHttpPageRequestControl httpRequestControl) {
        this.mHttpPageRequestControl = httpRequestControl;
    }

    public BaseObserver(IHttpRequestControl requestControl) {
        this.requestControl = requestControl;
    }

    public BaseObserver() {
        this(null, null);
    }

    public BaseObserver(IHttpPageRequestControl mHttpPageRequestControl, IHttpRequestControl requestControl) {
        this.mHttpPageRequestControl = mHttpPageRequestControl;
        this.requestControl = requestControl;
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onNext(T t) {
        onRequestSuccess(t);
    }

    /**
     * 获取成功后数据展示
     *
     * @param entity 可能为null
     */
    public abstract void onRequestSuccess(T entity);

    public void onRequestError(Throwable throwable) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(@NonNull Throwable e) {
        //错误全局拦截控制
        LogUtils.e(TAG + e.toString());
        boolean isIntercept = UiManager.getInstance().getObserverControl() != null && UiManager.getInstance().getObserverControl().onError(this, e);
        if (isIntercept) {
            return;
        }
        if (e instanceof DataNullException) {
            onNext(null);
            return;
        }
        if (UiManager.getInstance().getRequestControl() != null) {
            UiManager.getInstance().getRequestControl().httpRequestError(requestControl, e);
            LogUtils.e("执行了");
            return;
        }
        if (UiManager.getInstance().getHttpRequestPageControl() != null) {
            UiManager.getInstance().getHttpRequestPageControl().httpRequestError(mHttpPageRequestControl, e);
            LogUtils.e("执行了");
            return;
        }
        handleHttpError(e);
    }

    private void handleHttpError(Throwable e){
        if (e instanceof HttpException) {
            Response response = ((HttpException) e).response();
            ResponseBody body;
            if (response != null) {
                LogUtils.e("执行了");
                body = response.errorBody();
                if (body != null) {
                    try {
                        AbstractResult result = gson.fromJson(body.string(), AbstractResult.class);
                        if (result == null) {
                            onRequestError(e);
                            LogUtils.d("执行了");
                        } else {
                            switch (result.getStatus()) {
                                case REQUEST_CODE_TOKEN_INVALID:
                                    AccountHelper.getInstance().logoutAndSkipLogin();
                                    LogUtils.e("执行了");
                                    break;
                                default:
                                    LogUtils.e("执行了");
                                    onRequestSuccess((T) result);
                                    break;
                            }
                        }

                    } catch (IOException | JsonSyntaxException ex) {
                        ex.printStackTrace();
                        onRequestError(e);
                        LogUtils.e("执行了");
                    }
                }
            } else {
                LogUtils.e("执行了");
                onRequestError(e);
            }
        } else {
            LogUtils.e("执行了");
            onRequestError(e);
        }
    }
}
