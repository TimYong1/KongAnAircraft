package com.tourcoo.retrofit;


import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tourcoo.entity.AbstractResult;
import com.tourcoo.entity.BaseResultOld;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * @author :JenkinsZhou
 * @description : 拦截器
 * @company :途酷科技
 * @date 2020年11月25日9:40
 * @Email: 971613168@qq.com
 */
public class ResponseInterceptor implements Interceptor {

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
        AbstractResult result;
        try {
            result = new Gson().fromJson(respString, AbstractResult.class);
        } catch (JsonSyntaxException | ClassCastException e) {
            result = null;
            e.printStackTrace();
        }
        if (result != null && result.getStatus() == 401) {
        /*    ToastUtil.showNormal(result.getErrMsg());
            AccountHelper.getInstance().logout();*/
        //todo
        }
        return response;

    }
}
