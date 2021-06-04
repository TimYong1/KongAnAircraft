package com.tourcoo.retrofit;


import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tourcoo.config.AppConfig;
import com.tourcoo.entity.base.BaseCommonResult;
import com.tourcoo.entity.base.BaseSasResult;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

import static com.tourcoo.constant.CommonConstant.APP_TYPE_KONG_AN;
import static com.tourcoo.constant.CommonConstant.APP_TYPE_PRO;
import static com.tourcoo.constant.CommonConstant.APP_TYPE_SAS;

/**
 * @author :JenkinsZhou
 * @description : 拦截器
 * @company :途酷科技
 * @date 2020年11月25日9:40
 * @Email: 971613168@qq.com
 */
public class ResponseInterceptor implements Interceptor {
public static final String TAG = "ResponseInterceptor";
    @NotNull
    @Override
    public Response intercept(final Chain chain) throws IOException {
        // 原始请求
        Request request = chain.request();
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        if (responseBody == null || !RetrofitHelper.getInstance().isLogEnable()) {
            return response;
        }
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        String respString = source.getBuffer().clone().readString(Charset.defaultCharset());
        handleResponse(respString);
        LogUtils.i(TAG+respString+"---"+new Gson().toJson(request.headers()));
        return response;

    }


    private void handleResponse(String respString) {
        switch (AppConfig.APP_TYPE) {
            case APP_TYPE_KONG_AN:
            case APP_TYPE_PRO:
                BaseCommonResult baseCommonResult;
                try {
                    baseCommonResult = new Gson().fromJson(respString, BaseCommonResult.class);
                } catch (JsonSyntaxException | ClassCastException e) {
                    baseCommonResult = null;
                    e.printStackTrace();
                }
                if (baseCommonResult != null && baseCommonResult.getStatus() == 401) {
        /*    ToastUtil.showNormal(result.getErrMsg());
            AccountHelper.getInstance().logout();*/
                    //todo
                }

                break;
            case APP_TYPE_SAS:
                BaseSasResult result;
                try {
                    result = new Gson().fromJson(respString, BaseSasResult.class);
                } catch (JsonSyntaxException | ClassCastException e) {
                    result = null;
                    e.printStackTrace();
                }
             /*   if(result !=null && result.getStatus() == ){
                    ,
                }*/
                break;
        }
    }
}
