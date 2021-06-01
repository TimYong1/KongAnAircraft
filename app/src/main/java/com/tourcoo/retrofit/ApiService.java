package com.tourcoo.retrofit;


import com.tourcoo.account.TokenInfo;
import com.tourcoo.account.UserInfo;
import com.tourcoo.entity.BaseResult;
import com.tourcoo.entity.flight.FlightRecordEntity;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日11:15
 * @Email: 971613168@qq.com
 */
public interface ApiService {


    @POST("/api/app/auth/login")
    Observable<BaseResult<TokenInfo>> requestAppLogin(@Body Map<String, Object> map);

    /**
     * 获取直播地址
     * @param map
     * @return
     */
    @GET("/api/app/stream/getUrl")
    Observable<BaseResult<String>> requestStreamUrl(@QueryMap Map<String, Object> map);

    @DELETE("/api/app/auth/logout")
    Observable<BaseResult<Object>> requestLogout();


    @GET("/api/app/auth/info")
    Observable<BaseResult<Object>> requestUserInfo(@QueryMap Map<String, Object> map);

    /**
     * 设备信息
     * @param map
     * @return
     */
    @POST("/api/app/drone/uploadDroneData")
    Observable<BaseResult<Object>> requestUploadDroneData(@Body Map<String, Object> map);

    @POST("/api/app/auth/updatePass")
    Observable<BaseResult<Object>> requestEditPass(@Body Map<String, Object> map);


    @GET("/api/app/auth/info")
    Observable<BaseResult<UserInfo>> requestUserInfo();

    @POST("/api/flyRecord")
    Observable<BaseResult<FlightRecordEntity>> requestFlyRecord(@Body Map<String, Object> map);



}
