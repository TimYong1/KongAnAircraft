package com.tourcoo.retrofit;


import com.tourcoo.entity.account.SasTokenBean;
import com.tourcoo.entity.base.BaseSasResult;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import static com.tourcoo.retrofit.RetrofitHelper.HEADER_AUTHORIZATION;
import static com.tourcoo.retrofit.TokenInterceptor.HEADER_SAS_TENANT;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年10月28日11:17
 * @Email: 971613168@qq.com
 */
public interface TokenService {

    @Headers({TokenInterceptor.HEADER_NO_NEED_TOKEN})
    @POST("/api/oauth/noToken/login")
    Call<BaseSasResult> getNewToken(@Body Map<String, Object> map,@Header(HEADER_SAS_TENANT) String tenant, @Header(HEADER_AUTHORIZATION) String auth);
}
