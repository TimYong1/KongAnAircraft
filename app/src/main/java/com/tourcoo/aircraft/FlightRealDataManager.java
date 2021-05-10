package com.tourcoo.aircraft;


import com.apkfuns.logutils.LogUtils;
import com.tourcoo.aircraft.product.ProductManager;
import com.tourcoo.aircraft.product.AircraftUtil;
import com.tourcoo.entity.battery.BatteryData;
import com.tourcoo.entity.flight.AttitudeData;
import com.tourcoo.entity.flight.FlightRealTimeData;
import com.tourcoo.entity.flight.LocateData;


import dji.common.flightcontroller.BatteryThresholdBehavior;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.keysdk.BatteryKey;
import dji.keysdk.DJIKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

import static com.tourcoo.entity.battery.BatteryBehavior.BATTERY_FLY_NORMALLY;
import static com.tourcoo.entity.battery.BatteryBehavior.BATTERY_GO_HOME;
import static com.tourcoo.entity.battery.BatteryBehavior.BATTERY_LAND_IMMEDIATELY;
import static com.tourcoo.entity.battery.BatteryBehavior.BATTERY_STATE_FLY_NORMALLY;
import static com.tourcoo.entity.battery.BatteryBehavior.BATTERY_STATE_FLY_UNKNOWN;
import static com.tourcoo.entity.battery.BatteryBehavior.BATTERY_STATE_GO_HOME;
import static com.tourcoo.entity.battery.BatteryBehavior.BATTERY_STATE_LAND_IMMEDIATELY;
import static com.tourcoo.entity.battery.BatteryBehavior.BATTERY_UNKNOWN;


/**
 * @author :JenkinsZhou
 * @description : 飞行实时数据管理类
 * @company :途酷科技
 * @date 2021年03月16日15:44
 * @Email: 971613168@qq.com
 */
public class FlightRealDataManager {

    private static final double ERROR_VALUE = 0.0;
    private static final String TAG = "FlightRealDataManager";
    //垂直速度
    private FlightControllerKey velocityZ;
    //水平速度
    private FlightControllerKey velocityX;
    //垂直速度
    private FlightControllerKey velocityY;
    private FlightControllerKey visionPositioningEnabled;
    private FlightControllerKey isUltrasonicBeingUsed;
    private FlightControllerKey ultrasonicHeightInMeters;
    private DJIKey aircraftAltitudeKey;
    private BatteryKey remainBatteryPercentKey;
    private FlightRealTimeData realTimeData;
    private AttitudeData attitudeData;
    private BatteryData batteryData;
    private LocateData locateData;

    private void initKey() {
        velocityX = FlightControllerKey.create("VelocityX");
        velocityY = FlightControllerKey.create("VelocityY");
        velocityZ = FlightControllerKey.create("VelocityZ");
        aircraftAltitudeKey = FlightControllerKey.create("Altitude");
        visionPositioningEnabled = FlightControllerKey.createFlightAssistantKey("VisionPositioningEnabled");
        isUltrasonicBeingUsed = FlightControllerKey.create("IsUltrasonicBeingUsed");
        ultrasonicHeightInMeters = FlightControllerKey.create("UltrasonicHeightInMeters");
        remainBatteryPercentKey = BatteryKey.create("ChargeRemainingInPercent");
    }

    private float transformHorizontalSpeed(Object valueX, Object valueY) {
        float speedX;
        if (valueX == null) {
            speedX = 0;
        } else {
            speedX = (Float) valueX;
        }
        float speedY;
        if (valueY == null) {
            speedY = 0;
        } else {
            speedY = (Float) valueY;
        }
        float var10001 = speedX;
        var10001 *= var10001;
        float var10002 = speedY;
        return (float) Math.sqrt((double) (var10001 + var10002 * var10002));
    }

    private FlightRealDataManager() {
        initKey();
        realTimeData = new FlightRealTimeData();
        attitudeData = new AttitudeData();
        batteryData = new BatteryData();
        realTimeData.setAttitudeData(attitudeData);
    }

    private static class SingleHolder {
        private static FlightRealDataManager instance = new FlightRealDataManager();


    }

    public static FlightRealDataManager getInstance() {
        return SingleHolder.instance;
    }

    private void updateAttitude() {
        if (KeyManager.getInstance() == null) {
            return;
        }
        if (attitudeData == null) {
            attitudeData = new AttitudeData();
        }
        Object speedZ = KeyManager.getInstance().getValue(velocityZ);
        Object speedX = KeyManager.getInstance().getValue(velocityX);
        Object speedY = KeyManager.getInstance().getValue(velocityY);
        Object heightValue = KeyManager.getInstance().getValue(aircraftAltitudeKey);
        Float height = transformValue(heightValue, aircraftAltitudeKey);
        if (speedZ == null) {
            attitudeData.setVerticalSpeed(0f);
        } else {
            attitudeData.setVerticalSpeed((float) speedZ);
        }
        attitudeData.setHeight(height);
        attitudeData.setHorizontalSpeed(transformHorizontalSpeed(speedX, speedY));
        updateVps();
    }

    private void updateVps() {
        Object visionPositioningEnabledValue = KeyManager.getInstance().getValue(visionPositioningEnabled);
        Object isUltrasonicBeingUsedValue = KeyManager.getInstance().getValue(isUltrasonicBeingUsed);
        Object ultrasonicHeightInMetersValue = KeyManager.getInstance().getValue(ultrasonicHeightInMeters);
        boolean visionEnable;
        boolean ultrasonicBeingUsed;
        float heightMeter;
        if (visionPositioningEnabledValue == null) {
            visionEnable = false;
        } else {
            visionEnable = (boolean) visionPositioningEnabledValue;
        }
        if (isUltrasonicBeingUsedValue == null) {
            ultrasonicBeingUsed = false;
        } else {
            ultrasonicBeingUsed = (boolean) isUltrasonicBeingUsedValue;
        }
        if (ultrasonicHeightInMetersValue == null) {
            heightMeter = 0;
            LogUtils.w(TAG + "heightMeter = " + heightMeter);
        } else {
            heightMeter = (float) ultrasonicHeightInMetersValue;
            LogUtils.i(TAG + "heightMeter = " + heightMeter);
        }
        transformVPSValue(visionEnable, ultrasonicBeingUsed, heightMeter);
    }


    public FlightRealTimeData getRealTimeData() {
        if (realTimeData == null) {
            realTimeData = new FlightRealTimeData();
        }
        updateAttitude();
        updateLocateAndBattery();
        return realTimeData;
    }

    private Integer getBatteryRemainPercent() {
        Object percentValue = KeyManager.getInstance().getValue(remainBatteryPercentKey);
        if (percentValue == null) {
            return null;
        } else {
            return ((Integer) percentValue);
        }
    }

    private void transformVPSValue(boolean visionPositioningEnabled, boolean isUltrasonicBeingUsed, float height) {
        if (!visionPositioningEnabled) {
            attitudeData.setVps(null);
            LogUtils.i("执行了1");
        } else if (isUltrasonicBeingUsed) {
            attitudeData.setVps(height);
            LogUtils.i("执行了2");
           /* if (height <= 1.2F) {
                super.valueTextView.setTextColor(this.getResources().getColor(color.red));
            } else {
                super.valueTextView.setTextColor(this.getResources().getColor(color.white));
            }*/
        } else {
//            attitudeData.setVps(null);
            attitudeData.setVps(height);
        }
    }

    /**
     * 更新位置信息和电池电量信息
     */
    private void updateLocateAndBattery() {
        if (locateData == null) {
            locateData = new LocateData();
        }
        if (AircraftUtil.isFlightControllerAvailable()) {
            FlightController flightController = ((Aircraft) ProductManager.getProductInstance()).getFlightController();
            if (flightController != null) {
                FlightControllerState state = flightController.getState();
                updateBatteryInfo(state);
                LocationCoordinate3D locate = state.getAircraftLocation();
                setLocation(locate);
            } else {
                //todo 打log
                LogUtils.e(TAG + "flightController==null");
                setLocationUnknown();
            }
        } else {
            setLocationUnknown();
            LogUtils.e(TAG + "isFlightControllerAvailable=false");
        }
        realTimeData.setLocateData(locateData);
        realTimeData.setBatteryData(batteryData);
    }

    private void setLocationUnknown() {
        locateData.setLatitude(ERROR_VALUE);
        locateData.setLongitude(ERROR_VALUE);
    }

    private void setLocation(LocationCoordinate3D locationCoordinate3D) {
        if (locationCoordinate3D != null) {
//            LatLng latLng = getGDLatLng(locationCoordinate3D.getLatitude(), locationCoordinate3D.getLongitude());
            locateData.setLatitude(locationCoordinate3D.getLatitude());
            locateData.setLongitude(locationCoordinate3D.getLongitude());
        } else {
            LogUtils.d(TAG + "locationCoordinate3D==null");
            setLocationUnknown();
        }
    }

    private void updateBatteryInfo(FlightControllerState flightControllerState) {
        if (batteryData == null) {
            batteryData = new BatteryData();
        }
        if (flightControllerState != null) {
            BatteryThresholdBehavior battery = flightControllerState.getBatteryThresholdBehavior();
            if (battery != null) {
                int state;
                switch (battery.toString()) {
                    case BATTERY_FLY_NORMALLY:
                        state = BATTERY_STATE_FLY_NORMALLY;
                        break;
                    case BATTERY_GO_HOME:
                        state = BATTERY_STATE_GO_HOME;
                        break;
                    case BATTERY_LAND_IMMEDIATELY:
                        state = BATTERY_STATE_LAND_IMMEDIATELY;
                        break;
                    case BATTERY_UNKNOWN:
                    default:
                        state = BATTERY_STATE_FLY_UNKNOWN;
                        break;
                }
                Integer remain = getBatteryRemainPercent();
                batteryData.setRemainPercent(remain);
                batteryData.setBatteryWarningType(state);
            }
        }
    }

    // 坐标转换-转为高德坐标系(高德API)
  /*  private LatLng getGDLatLng(double lat, double lng) {
        CoordinateConverter converter = new CoordinateConverter(FlyApplication.getContext());
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标点 DPoint类型
        converter.coord(new LatLng(lat, lng));
        // 执行转换操作
        LatLng desLatLng = converter.convert();
        return new LatLng(desLatLng.latitude, desLatLng.longitude);
    }*/

    public Float transformValue(Object value, DJIKey key) {
        if (key.equals(this.aircraftAltitudeKey)) {
            if (value != null) {
                return (Float) value;
            }
            return null;
        }
        return null;
    }
}
