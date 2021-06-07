package com.tourcoo.retrofit;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;


import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tourcoo.account.AccountHelper;
import com.tourcoo.aircraft.ui.account.LoginNewActivity;
import com.tourcoo.entity.account.SasTokenBean;
import com.tourcoo.entity.base.BaseSasResult;
import com.tourcoo.util.CommonUtil;
import com.tourcoo.util.StackUtil;
import com.tourcoo.util.ToastUtil;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.codec.Base64;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.tourcoo.retrofit.RetrofitHelper.VALUE_AUTH;


/**
 * @author :JenkinsZhou
 * @description :token拦截器
 * @company :途酷科技
 * @date 2019年10月29日9:54
 * @Email: 971613168@qq.com
 */
public class TokenInterceptor implements Interceptor {
    private static final String TAG = "TokenInterceptor";
    private static int ACTIVITY_SINGLE_FLAG = Intent.FLAG_ACTIVITY_SINGLE_TOP;
    public static final String HEADER_TOKEN = "token";
    public static final String HEADER_SAS_TENANT = "tenant";
    public static final String BEARER = "Bearer ";

    private static final String TOKEN_FLAG = "NeedToken";
    private static final String SKIP_LOGIN_FLAG = "skipLogin";
    private static final String YES = "true";
    private static final String NO = "false";
    public static final String HEADER_NEED_TOKEN = TOKEN_FLAG + ": " + YES;
    public static final String HEADER_NO_NEED_TOKEN = TOKEN_FLAG + ": " + NO;
    public static final String HEADER_NOT_SKIP_LOGIN = SKIP_LOGIN_FLAG + ": " + NO;
    public static final String HEADER_SKIP_LOGIN = SKIP_LOGIN_FLAG + ": " + YES;
    public static final String URL_REFRESH_TOKEN = RequestConfig.BASE_SAS_URL;
    public static final int CODE_REQUEST_TOKEN_INVALID = 40001;
    public static final int CODE_REQUEST_TOKEN_4009 = 40009;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String tokenFlag = originalRequest.header(TOKEN_FLAG);
        if (tokenFlag == null || tokenFlag.equalsIgnoreCase(NO)) {
            LogUtils.i(TAG + "不需要token验证 不做任何处理");
            Request newTokenRequest = chain.request().newBuilder()
                    .removeHeader(HEADER_TOKEN)
                    .build();
            return chain.proceed(newTokenRequest);
        } else {
            String skipLoginFlag = originalRequest.header(SKIP_LOGIN_FLAG);
            boolean skipLoginEnable = skipLoginFlag == null || skipLoginFlag.equalsIgnoreCase(YES);
            LogUtils.w(TAG + "需要token验证");
            //需要token校验
            Request tokenRequest = chain.request().newBuilder()
                    .removeHeader(HEADER_TOKEN)
                    .addHeader(HEADER_TOKEN, BEARER + AccountHelper.getInstance().getSysToken())
                    .build();
            LogUtils.w(TAG + "tokenRequest携带的token=" + AccountHelper.getInstance().getSysToken());
            Response tokenResponse = chain.proceed(tokenRequest);
            ResponseBody responseBody = tokenResponse.body();
            if (responseBody == null) {
                return tokenResponse;
            }
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            String respString = source.getBuffer().clone().readString(Charset.defaultCharset());
            BaseSasResult baseSasResult;
            try {
                baseSasResult = new Gson().fromJson(respString, BaseSasResult.class);
            } catch (JsonSyntaxException | ClassCastException e) {
                baseSasResult = null;
                e.printStackTrace();
            }
            boolean condition = baseSasResult != null && ("40008".equalsIgnoreCase("" + baseSasResult.getCode()));
            if (condition) {
                if (skipLoginEnable) {
                    skipLogin();
                }
                ToastUtil.showFailed(baseSasResult.getMsg());
                AccountHelper.getInstance().logout();
                return tokenResponse;
            } else {
                boolean condition1 = baseSasResult != null && (!"40008".equalsIgnoreCase("" + baseSasResult.getCode())) && ("" + baseSasResult.getCode()).startsWith("400");
                if (condition1) {
                    //说明token过期
                    LogUtils.e(TAG + "tokenRequest携带的token已经失效,需要重新请求");
                    SasTokenBean tokenInfo = getNewToken();
                    if (tokenInfo == null) {

                        if (skipLoginEnable) {
                            skipLogin();
                        }
                        AccountHelper.getInstance().logout();
                        //todo
//                    EventBus.getDefault().post(new UserInfoEvent());
                        return chain.proceed(tokenRequest);
                    } else {
                        //token刷新成功
                        LogUtils.i(TAG + "获取新token成功--->" + tokenInfo.getToken());
                        AccountHelper.getInstance().setSysToken(tokenInfo.getToken());
                        AccountHelper.getInstance().setRefreshToken(tokenInfo.getRefreshToken());
                        Request newTokenRequest = chain.request().newBuilder()
                                .removeHeader(HEADER_TOKEN)
                                .addHeader(HEADER_TOKEN, BEARER + tokenInfo.getToken())
                                .build();
                        LogUtils.i(TAG + "newTokenRequest携带的token--->" + tokenInfo.getToken());
                        return chain.proceed(newTokenRequest);
                    }
                } else {
                    LogUtils.i(TAG + "当前token在有效期内 验证通过 不需要刷新token~：携带的token是tokenResponse中的=" + AccountHelper.getInstance().getSysToken());
                    return tokenResponse;
                }
            }
        }
    }


    private SasTokenBean getNewToken() {
//        OkHttpClient.Builder okHttpB = new OkHttpClient.Builder().addInterceptor(new ResponseInterceptor());
        Retrofit retrofit = new Retrofit.Builder()
                //基础url,其他部分在GetRequestInterface里
                .baseUrl(URL_REFRESH_TOKEN)
                //Gson数据转换器
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //创建网络请求接口实例
        TokenService apiService = retrofit.create(TokenService.class);
        LogUtils.i(TAG + "获取新token传入的参数:" + AccountHelper.getInstance().getRefreshToken());
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("refreshToken", AccountHelper.getInstance().getRefreshToken());
        hashMap.put("grantType", "refresh_token");
        String authValue = Base64.encode(VALUE_AUTH);
        String sasEncode = Base64.encode(AccountHelper.getInstance().getSasTenant());
        Call<BaseSasResult> call = apiService.getNewToken(hashMap, sasEncode, "Basic " + authValue);
        try {
            BaseSasResult baseSasResult = call.execute().body();
            LogUtils.e(TAG + new Gson().toJson(baseSasResult));
            if (baseSasResult != null) {
                return parseJavaBean(baseSasResult.data, SasTokenBean.class);
            } else {
                LogUtils.e(TAG + "refreshToken都已经失效了 只能重新登录了");
            }
        } catch (Exception ex) {
            LogUtils.e(TAG + "getNewToken()报错-->" + ex.getMessage());
        }
        return null;
    }


    private <T> T parseJavaBean(Object data, Class<T> tClass) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(gson.toJson(data), tClass);
        } catch (Exception e) {
            LogUtils.e("parseJavaBean()报错--->" + e.toString());
            return null;
        }
    }


    private void skipLogin() {
        AccountHelper.getInstance().logout();
        StackUtil.getInstance().popAll();
        CommonUtil.startActivity(CommonUtil.getApplication(), LoginNewActivity.class);
    }


}
