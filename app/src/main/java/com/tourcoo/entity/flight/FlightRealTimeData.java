package com.tourcoo.entity.flight;


import com.tourcoo.entity.battery.BatteryData;

/**
 * @author :JenkinsZhou
 * @description : 无人机飞行数据
 * @company :途酷科技
 * @date 2021年03月16日14:00
 * @Email: 971613168@qq.com
 */
public class FlightRealTimeData {
    private LocateData locateData;
    private AttitudeData attitudeData;
    private BatteryData batteryData;
    private LocateData userLocateData;

    public LocateData getLocateData() {
        return locateData;
    }

    public void setLocateData(LocateData locateData) {
        this.locateData = locateData;
    }

    public AttitudeData getAttitudeData() {
        return attitudeData;
    }

    public void setAttitudeData(AttitudeData attitudeData) {
        this.attitudeData = attitudeData;
    }

    public BatteryData getBatteryData() {
        return batteryData;
    }

    public void setBatteryData(BatteryData batteryData) {
        this.batteryData = batteryData;
    }

    public LocateData getUserLocateData() {
        return userLocateData;
    }

    public void setUserLocateData(LocateData userLocateData) {
        this.userLocateData = userLocateData;
    }
}
