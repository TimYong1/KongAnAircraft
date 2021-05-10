package com.tourcoo.manager;

/**
 * @author :JenkinsZhou
 * @description : 飞行器相关帮助类
 * @company :途酷科技
 * @date 2021年03月25日13:39
 * @Email: 971613168@qq.com
 */
public class AircraftHelper {

    private boolean upload = false;


    private static class Holder {
        private static AircraftHelper instance = new AircraftHelper();

    }

    private AircraftHelper() {

    }

    public static AircraftHelper getInstance() {
        return Holder.instance;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }
}

