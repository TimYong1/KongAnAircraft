package com.tourcoo.retrofit.repository;


import com.apkfuns.logutils.LogUtils;
import com.tourcoo.account.TokenInfo;
import com.tourcoo.account.UserInfo;
import com.tourcoo.entity.base.BaseCommonResult;
import com.tourcoo.entity.flight.FlightRecordEntity;
import com.tourcoo.retrofit.ApiService;
import com.tourcoo.retrofit.RetrofitHelper;
import com.tourcoo.retrofit.RetryWhen;
import com.tourcoo.retrofit.ThreadTransformer;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日11:14
 * @Email: 971613168@qq.com
 */
public class ApiRepository extends BaseRepository {

    private static volatile ApiRepository instance;
    private ApiService mApiService;

    private ApiRepository() {
        mApiService = getApiService();
    }

    public static ApiRepository getInstance() {
        if (instance == null) {
            synchronized (ApiRepository.class) {
                if (instance == null) {
                    instance = new ApiRepository();
                }
            }
        }
        return instance;
    }


    public ApiService getApiService() {
        mApiService = RetrofitHelper.getInstance().createService(ApiService.class);
        return mApiService;
    }


    /**
     * 系统配置
     *
     * @return
     */
    public Observable<BaseCommonResult<TokenInfo>> requestAppLogin(String userName, String pass) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("username", userName);
        params.put("password", pass);
        return ThreadTransformer.switchSchedulers(getApiService().requestAppLogin(params).retryWhen(new RetryWhen()));
    }


    public Observable<BaseCommonResult<String>> requestStreamUrl(Map<String, Object> params) {
        LogUtils.tag("提交到服务器的数据").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestStreamUrl(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseCommonResult<Object>> requestLogout() {
        return ThreadTransformer.switchSchedulers(getApiService().requestLogout().retryWhen(new RetryWhen()));
    }

    public Observable<BaseCommonResult<Object>> requestUploadDroneData(Map<String, Object> params) {
        LogUtils.tag("提交到服务器的数据").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestUploadDroneData(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseCommonResult<Object>> requestEditPass(String oldPass, String newPass) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("oldPass", oldPass);
        params.put("newPass", newPass);
        LogUtils.tag("提交到服务器的数据").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestEditPass(params).retryWhen(new RetryWhen()));
    }


    public Observable<BaseCommonResult<UserInfo>> requestUserInfo() {
        return ThreadTransformer.switchSchedulers(getApiService().requestUserInfo().retryWhen(new RetryWhen()));
    }


    public Observable<BaseCommonResult<FlightRecordEntity>> requestFlyRecord(Map<String, Object> params) {
        /*{
            "address": "",
                "appUserId": 0,
                "droneId": 0,
                "flightTime": "",
                "id": 0,
                "landTime": "",
                "takeTime": ""
        }*/
        LogUtils.tag("提交到服务器的数据").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestFlyRecord(params).retryWhen(new RetryWhen()));
    }


}
