package com.tourcoo.aircraft.ui.map;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraftmanager.R;
import com.tourcoo.threadpool.ThreadManager;
import com.tourcoo.util.LocateHelper;
import com.tourcoo.util.SpUtil;
import com.tourcoo.util.ToastUtil;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.flyzone.FlyZoneInformation;
import dji.common.flightcontroller.flyzone.SubFlyZoneInformation;
import dji.common.flightcontroller.flyzone.SubFlyZoneShape;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlyZoneManager;
import dji.sdk.sdkmanager.DJISDKManager;

import static com.tourcoo.constant.LocateConstant.PREF_KEY_LAST_LOCATE_LANG;
import static com.tourcoo.constant.LocateConstant.PREF_KEY_LAST_LOCATE_LAT;

/**
 * @author :JenkinsZhou
 * @description :禁飞区
 * @company :途酷科技
 * @date 2021年05月17日17:07
 * @Email: 971613168@qq.com
 */
public class MapActivity extends RxAppCompatActivity implements AMapLocationListener, LocationSource {
    private MapView mapView;
    private AMap aMap;
    public static final String TAG = "MapActivity";
    private boolean isFirstLoc = true;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private final int[] shapeColors = {Color.parseColor("#EF5849"), Color.parseColor("#B6B5BA"),};
    private final int[] fillColors = {Color.parseColor("#80EF5849"), Color.parseColor("#80B6B5BA"),};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        aMap = mapView.getMap();
        //开始定位
        initLocate();
        locate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        locate();
        updateFlyZone();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }


    private void getCurrentLocateAndSetMap() {
        ThreadManager.getDefault().execute(new Runnable() {
            @Override
            public void run() {
                Double lastLat = SpUtil.INSTANCE.getDouble(PREF_KEY_LAST_LOCATE_LAT);
                Double lastLang = SpUtil.INSTANCE.getDouble(PREF_KEY_LAST_LOCATE_LANG);
                if (lastLat != null && lastLang != null) {
                    LatLng latLng = new LatLng(lastLat, lastLang);
                    //如果存在上次定位记录 则先用上次位置
//                    setCurrentLatLngOnMap(latLng);
                }
                LocateHelper.getInstance().startLocate(getApplicationContext(), new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation aMapLocation) {
                        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                            LogUtils.i(TAG + "定位成功");
                            double lat = aMapLocation.getLatitude();
                            double lang = aMapLocation.getLongitude();
                            LatLng latLng = new LatLng(lat, lang);
                           /* setCurrentLatLngOnMap(latLng);
                            saveCurrentLatLangToLocal(lat, lang);
                            if (userLocate == null) {
                                userLocate = new LocateData();
                            }
                            userLocate.setLongitude(lang);
                            userLocate.setLatitude(lat);*/
                        } else {
                            LogUtils.e(TAG + "定位失败");
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        if (mapView != null) {
            mapView.onPause();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }


    //定位
    private void locate() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                amapLocation.getCityCode();//城市编码
                amapLocation.getAdCode();//地区编码

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    float level = aMap.getMaxZoomLevel();
                    if (level > 3) {
                        level = level - 3;
                    }
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(level));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
                    //添加图钉
//                    aMap.addMarker(getMarkerOptions(amapLocation));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
//                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }
                if (mLocationClient != null) {
                    mLocationClient.stopLocation();
                }

            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                LogUtils.e("AmapError" + "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());

                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }


    //自定义一个图钉，并且设置图标，当我们点击图钉时，显示设置的信息
    private MarkerOptions getMarkerOptions(AMapLocation amapLocation) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_locate_arrow));
        //位置
        options.position(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
        StringBuffer buffer = new StringBuffer();
        buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
        //标题
        options.title(buffer.toString());
        //子标题
        options.snippet("这里好火");
        //设置多少帧刷新一次图片资源
        options.period(60);

        return options;

    }

    private void release() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.stopAssistantLocation();
            mLocationClient.unRegisterLocationListener(this);
            mLocationClient.onDestroy();
            aMap.clear();
            mListener = null;
            mLocationOption = null;
            mLocationClient = null;
        }
    }


    private void initLocate() {
        //设置显示定位按钮 并且可以点击
        UiSettings settings = aMap.getUiSettings();
        //设置定位监听
        aMap.setLocationSource(this);
        // 是否显示定位按钮
        settings.setMyLocationButtonEnabled(true);
        // 是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);
        //定位的小图标 默认是蓝点 这里自定义一团火，其实就是一张图片
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_locate_arrow));
        myLocationStyle.radiusFillColor(android.R.color.transparent);
        myLocationStyle.strokeColor(android.R.color.transparent);
        aMap.setMyLocationStyle(myLocationStyle);
    }


    private void updateFlyZone() {
        FlyZoneManager fzMgr = DJISDKManager.getInstance().getFlyZoneManager();
        fzMgr.getFlyZonesInSurroundingArea(new CommonCallbacks.CompletionCallbackWith<ArrayList<FlyZoneInformation>>() {
            @Override
            public void onSuccess(ArrayList<FlyZoneInformation> flyZoneInformationList) {
                ToastUtil.showSuccessDebug("禁飞区信息获取成功:" + flyZoneInformationList.size());
                aMap.clear();
                //获取周边飞行区域列表成功
                for (FlyZoneInformation flyZoneInformation : flyZoneInformationList) {
                    switch (flyZoneInformation.getCategory()) {
                        //禁飞区
                        case RESTRICTED:
                            //先判断禁飞区区域形状
                            switch (flyZoneInformation.getFlyZoneType()) {
                                case POLY:
                                    //多区域型
                                    SubFlyZoneInformation[] subFlyZones = flyZoneInformation.getSubFlyZones();
                                    //开始遍历子区域
                                    for (int i = 0; i != subFlyZones.length; ++i) {
                                        if (subFlyZones[i].getShape() == SubFlyZoneShape.POLYGON) {
                                            List<LocationCoordinate2D> coordinate2DList = subFlyZones[i].getVertices();
                                            //当子区域形状为多边形时
                                            //此时可通过subFlyZones[i].getVertices()获取多边形节点
//                                            subFlyZones[i].getMaxFlightHeight()获取最大飞行高度
//                                            drawShape(Color.parseColor(color1), Color.parseColor(color), coordinate2DList);
                                            drawShape(shapeColors[(i % shapeColors.length)], fillColors[(i % fillColors.length)], coordinate2DList);
                                        } else if (subFlyZones[i].getShape() == SubFlyZoneShape.CYLINDER) {
                                            //当子区域形状为圆柱时
                                            //此时subFlyZones.getRadius();获取圆柱地面原的半径
                                            drawCircle(shapeColors[(i % shapeColors.length)], fillColors[(i % fillColors.length)], subFlyZones[i].getCenter(), subFlyZones[i].getRadius());
                                        }
                                    }
                                    break;
                                case CIRCLE:
                                    //当区域形状为圆形时
                                    //此时可通过flyZone.getCoordinate()获取中心位置坐标
                                    drawCircle(shapeColors[1], fillColors[1], flyZoneInformation.getCoordinate(), flyZoneInformation.getRadius());
                                    break;
                            }

                            break;
                    }

                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                ToastUtil.showNormalDebug("禁飞区信息获取失败");
            }
        });
    }


    /**
     * 绘制圆圈
     *
     * @param coordinate2D
     * @param radius
     */
    public void drawCircle(int shapeColor, int fillColor, LocationCoordinate2D coordinate2D, double radius) {
        if (coordinate2D == null) {
            return;
        }
        LatLng latLng = new LatLng(coordinate2D.getLatitude(), coordinate2D.getLongitude());
        /*StringBuilder sb = new StringBuilder(color);// 构造一个StringBuilder对象
        sb.insert(1, "该区域限制飞行");// 在指定的位置10，插入指定的字符串*/
        aMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .fillColor(shapeColor)
                .strokeColor(fillColor)
                .strokeWidth(5));
    }


    // 画区域
    private void drawShape(int strokeColor, int fillColor, List<LocationCoordinate2D> locationCoordinate2DList) {
        if (locationCoordinate2DList == null || locationCoordinate2DList.isEmpty()) {
            return;
        }
        List<LatLng> areas = new ArrayList<>();
        for (LocationCoordinate2D locationCoordinate2D : locationCoordinate2DList) {
            if (locationCoordinate2D != null) {
                areas.add(new LatLng(locationCoordinate2D.getLatitude(), locationCoordinate2D.getLongitude()));
            }
        }
        // 定义多边形的属性信息
        PolygonOptions polygonOptions = new PolygonOptions();
        // 添加多个多边形边框的顶点
        for (LatLng latLng : areas) {
            polygonOptions.add(latLng);
        }
        // 设置多边形的边框颜色，32位 ARGB格式，默认为黑色
        polygonOptions.strokeColor(strokeColor);
        // 设置多边形的边框宽度，单位：像素
        polygonOptions.strokeWidth(5);
        // 设置多边形的填充颜色，32位ARGB格式
        polygonOptions.fillColor(fillColor); // 注意要加前两位的透明度
        // 在地图上添加一个多边形（polygon）对象
        aMap.addPolygon(polygonOptions);
    }


}
