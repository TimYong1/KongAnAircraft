package com.tourcoo.util;

import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * @author :JenkinsZhou
 * @description : 定位工具类
 * @company :途酷科技
 * @date 2021年02月05日14:35
 * @Email: 971613168@qq.com
 */
public class LocateHelper {
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    private AMapLocationListener mLocationListener;

    public void startLocate(Context context, AMapLocationListener locationListener ) {
        this.mLocationListener = locationListener;
        mLocationClient = new AMapLocationClient(context);
        AMapLocationClientOption option = new AMapLocationClientOption();
        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        if (null != mLocationClient) {
            mLocationClient.setLocationOption(option);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.setLocationListener(mLocationListener);
            mLocationClient.startLocation();
        }


    }


    public void release() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
        mLocationListener = null;
    }


    private LocateHelper() {
    }

    public static LocateHelper getInstance() {
        return InstanceHolder.instance;
    }

    private static class InstanceHolder {
        private static LocateHelper instance = new LocateHelper();
    }

}
