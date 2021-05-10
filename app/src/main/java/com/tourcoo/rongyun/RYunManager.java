package com.tourcoo.rongyun;

/**
 * @author :JenkinsZhou
 * @description : 融云相关管理
 * @company :途酷科技
 * @date 2021年03月22日15:07
 * @Email: 971613168@qq.com
 */
public class RYunManager {

    private static boolean connected = false;

    public static boolean isConnected() {
        return connected;
    }

    public static void setConnected(boolean connected) {
        RYunManager.connected = connected;
    }
}
