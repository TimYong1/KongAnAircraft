package com.tourcoo.entity.battery;

/**
 * @author :JenkinsZhou
 * @description : 电池预警相关常量
 * @company :途酷科技
 * @date 2021年03月17日10:32
 * @Email: 971613168@qq.com
 */
public class BatteryBehavior {

    /**
     * FLY_NORMALLY(0),
     * GO_HOME(1),
     * LAND_IMMEDIATELY(2),
     * UNKNOWN(255);
     */
    public static final String BATTERY_FLY_NORMALLY = "FLY_NORMALLY";
    public static final String BATTERY_GO_HOME = "GO_HOME";
    public static final String BATTERY_LAND_IMMEDIATELY = "LAND_IMMEDIATELY";
    public static final String BATTERY_UNKNOWN = "UNKNOWN";

    public static final int BATTERY_STATE_FLY_NORMALLY = 0;
    public static final int BATTERY_STATE_GO_HOME = 1;
    public static final int BATTERY_STATE_LAND_IMMEDIATELY = 2;
    public static final int BATTERY_STATE_FLY_UNKNOWN = 255;

}
