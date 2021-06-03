package com.tourcoo.control.impl;

import android.accounts.AccountsException;
import android.accounts.NetworkErrorException;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.tourcoo.account.AccountHelper;
import com.tourcoo.aircraft.ui.sample.AircraftApplication;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.control.HttpRequestControl;
import com.tourcoo.control.IHttpRequestControl;
import com.tourcoo.entity.BaseCommonResult;
import com.tourcoo.retrofit.NetworkUtil;
import com.tourcoo.retrofit.RequestConfig;
import com.tourcoo.util.ToastUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

import static com.tourcoo.retrofit.RequestConfig.REQUEST_CODE_TOKEN_INVALID;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年12月22日11:10
 * @Email: 971613168@qq.com
 */
public class HttpRequestControlImpl implements HttpRequestControl {
    private static String TAG = "HttpRequestControlImpl";
    private Gson gson = new Gson();

    @Override
    public void httpRequestSuccess(IHttpRequestControl httpRequestControl, BaseCommonResult<?> data) {
        LogUtils.tag(TAG).i("执行了1");
        if (httpRequestControl == null) {
            Toast.makeText(AircraftApplication.getContext(), "httpRequestControl=null", Toast.LENGTH_SHORT).show();
            return;
        }
        LogUtils.tag(TAG).i("执行了2");
        //todo
      /*  LoadService statusLayoutManager = httpRequestControl.getStatusLayoutManager();
        if (statusLayoutManager == null) {
            LogUtils.e(TAG+"未获取到多状态管理实例");
            return;
        }
        if (data == null) {
            statusLayoutManager.showCallback(MultiStatusErrorCallback.class);
            return;
        }
        if (data.getCode() != RequestConfig.REQUEST_CODE_SUCCESS) {
//            ToastUtil.showNormal(data.getErrMsg());
            return;
        }

        httpRequestControl.handleSuccessData(data);
        statusLayoutManager.showSuccess();*/
        if (data.getStatus() != RequestConfig.REQUEST_CODE_SUCCESS) {
            Toast.makeText(AircraftApplication.getContext(), "data=" + data.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        httpRequestControl.handleSuccessData(data);
    }

    @Override
    public void httpRequestError(IHttpRequestControl httpRequestControl, Throwable e) {
        int reason = R.string.exception_other_error;
        LogUtils.e(TAG+e.toString());
        if (!NetworkUtil.isConnected(AircraftApplication.getContext())) {
            reason = R.string.exception_network_not_connected;
        } else {
            //网络异常--继承于AccountsException
            if (e instanceof NetworkErrorException) {
                reason = R.string.exception_network_error;
                //账户异常
            } else if (e instanceof AccountsException) {
                reason = R.string.exception_accounts;
                //连接异常--继承于SocketException
            } else if (e instanceof ConnectException) {
                reason = R.string.exception_connect;
                //socket异常
            } else if (e instanceof SocketException) {
                reason = R.string.exception_socket;
                // http异常
            } else if (e instanceof UnknownHostException) {
                reason = R.string.exception_unknown_host;
            } else if (e instanceof JsonParseException) {
                //数据格式化错误
                reason = R.string.exception_json_syntax;
            } else if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
                reason = R.string.exception_time_out;
            } else if (e instanceof ClassCastException) {
                reason = R.string.exception_class_cast;
            } else if (e instanceof HttpException) {
                handleHttpError(e);
            }
        }
        boolean needShowToast = !(e instanceof HttpException) && httpRequestControl == null;
        LogUtils.w("是否显示:" + needShowToast);
        if (needShowToast) {
            ToastUtil.showFailed(reason);
            return;
        }
        //todo
       /* else if (e instanceof HttpException) {
            if (e.toString().contains("" + RequestConfig.REQUEST_CODE_TOKEN_INVALID)) {
                reason = R.string.exception_accounts;
                AccountHelper.getInstance().logout();
            }else {
                reason = R.string.exception_http;
            }
            //DNS错误
        } */
      /*  LoadService statusManager = httpRequestControl.getStatusLayoutManager();
        if (!NetworkUtil.isConnected(FlyApplication.getContext())) {
            statusManager.showCallback(MultiStatusNetErrorCallback.class);
            return;
        }
        statusManager.showCallback(MultiStatusErrorCallback.class);*/
    }

    private void handleHttpError(Throwable e) {
        if (e instanceof HttpException) {
            Response response = ((HttpException) e).response();
            ResponseBody body;
            if (response != null) {
                LogUtils.e("执行了");
                body = response.errorBody();
                if (body != null) {
                    try {
                        BaseCommonResult result = gson.fromJson(body.string(), BaseCommonResult.class);
                        if (result == null) {
                            ToastUtil.showFailed(R.string.exception_network_data_error);
                            LogUtils.d("执行了");
                        } else {
                            switch (result.status) {
                                case REQUEST_CODE_TOKEN_INVALID:
                                   AccountHelper.getInstance().logoutAndSkipLogin();
                                    LogUtils.e("执行了");
                                    break;
                                default:
                                    LogUtils.i("执行了");
                                    ToastUtil.showFailed(result.getMessage());
                                    break;
                            }
                        }

                    } catch (IOException | JsonSyntaxException ex) {
                        ex.printStackTrace();
                        ToastUtil.showFailedDebug(ex.toString());
                        LogUtils.e("执行了");
                    }
                }
            } else {
                LogUtils.e("执行了");
                ToastUtil.showFailedDebug(e.toString());
            }
        } else {
            LogUtils.e("执行了");
            ToastUtil.showFailedDebug(e.toString());
        }
    }
}
