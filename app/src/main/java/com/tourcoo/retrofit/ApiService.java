package com.tourcoo.retrofit;


import com.tourcoo.account.SasUserInfo;
import com.tourcoo.entity.account.SasTokenBean;
import com.tourcoo.entity.base.BaseSasResult;
import com.tourcoo.entity.flight.FlightRecordEntity;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日11:15
 * @Email: 971613168@qq.com
 */
public interface ApiService {


    @Headers({TokenInterceptor.HEADER_NO_NEED_TOKEN})
    @POST("/api/oauth/noToken/login")
    Observable<BaseSasResult<SasTokenBean>> requestAppLogin(@Body Map<String, Object> map);

    /**
     * 获取直播地址
     * @return
     */
    @Headers({TokenInterceptor.HEADER_NEED_TOKEN,TokenInterceptor.HEADER_SKIP_LOGIN})
    @GET("/api/business/stream/address/{droneId}")
    Observable<BaseSasResult<String>> requestStreamUrl(@Path("droneId") String droneId);

    /**
     * 退出登录
     * @return
     */
    @POST("/api/oauth/noToken/logout")
    Observable<BaseSasResult<Object>> requestLogout();


    @Headers({TokenInterceptor.HEADER_NEED_TOKEN,TokenInterceptor.HEADER_SKIP_LOGIN})
    @GET("/api/app/auth/info")
    Observable<BaseSasResult<Object>> requestUserInfo(@QueryMap Map<String, Object> map);

    /**
     * 新增无人机设备信息
     * @param map
     * @return
     */
    @Headers({TokenInterceptor.HEADER_NEED_TOKEN,TokenInterceptor.HEADER_SKIP_LOGIN})
    @POST("/api/business/drone/add")
    Observable<BaseSasResult<Object>> requestUploadDroneData(@Body Map<String, Object> map);


    @Headers({TokenInterceptor.HEADER_NEED_TOKEN,TokenInterceptor.HEADER_SKIP_LOGIN})
    @POST("/api/app/auth/updatePass")
    Observable<BaseSasResult<Object>> requestEditPass(@Body Map<String, Object> map);

    @Headers({TokenInterceptor.HEADER_NEED_TOKEN,TokenInterceptor.HEADER_SKIP_LOGIN})
    @GET("/api/authority/user/{id}")
    Observable<BaseSasResult<SasUserInfo>> requestUserInfo(@Path("id") String userId);

    @Headers({TokenInterceptor.HEADER_NEED_TOKEN,TokenInterceptor.HEADER_SKIP_LOGIN})
    @POST("/api/flyRecord")
    Observable<BaseSasResult<FlightRecordEntity>> requestFlyRecord(@Body Map<String, Object> map);

    @Headers({TokenInterceptor.HEADER_NEED_TOKEN,TokenInterceptor.HEADER_SKIP_LOGIN})
    @GET("/api/business/rongCloud/getToken")
    Observable<BaseSasResult<String>> requestRongYunToken();


}
