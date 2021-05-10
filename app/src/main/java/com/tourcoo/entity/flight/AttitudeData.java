package com.tourcoo.entity.flight;

/**
 * @author :JenkinsZhou
 * @description : 无人机姿态数据
 * @company :途酷科技
 * @date 2021年03月16日14:25
 * @Email: 971613168@qq.com
 */
public class AttitudeData {


    /**
     * D 0 M：飞行器与返航点水平方向的距离；
     */
    private float distance;


    /**
     * H 0 M：飞行器与返航点垂直方向的距离；
     */
    private Float height;

    /**
     * 飞行器垂直速度
     */
    private float verticalSpeed;

    /**
     * 飞行器水平速度
     */
    private float horizontalSpeed;

    /**
     * 飞行器相对地面的高度
     */
    private Float vps;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public float getVerticalSpeed() {
        return verticalSpeed;
    }

    public void setVerticalSpeed(float verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    public float getHorizontalSpeed() {
        return horizontalSpeed;
    }

    public void setHorizontalSpeed(float horizontalSpeed) {
        this.horizontalSpeed = horizontalSpeed;
    }

    public Float getVps() {
        return vps;
    }

    public void setVps(Float vps) {
        this.vps = vps;
    }
}
