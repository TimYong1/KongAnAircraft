package com.tourcoo.entity.battery;

/**
 * @author :JenkinsZhou
 * @description : 电池数据
 * @company :途酷科技
 * @date 2021年03月16日14:26
 * @Email: 971613168@qq.com
 */
public class BatteryData {
    /**
     *
     */
    private Integer remainPercent;
    /**
     * 电池预警类型
     */
    private int batteryWarningType;

    public Integer getRemainPercent() {
        return remainPercent;
    }

    public void setRemainPercent(Integer remainPercent) {
        this.remainPercent = remainPercent;
    }

    public int getBatteryWarningType() {
        return batteryWarningType;
    }

    public void setBatteryWarningType(int batteryWarningType) {
        this.batteryWarningType = batteryWarningType;
    }
}
