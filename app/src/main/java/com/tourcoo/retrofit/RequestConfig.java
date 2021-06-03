package com.tourcoo.retrofit;

/**
 * @author :JenkinsZhou
 * @description :请求相关常量
 * @company :途酷科技
 * @date 2020年10月28日11:22
 * @Email: 971613168@qq.com
 */
public class RequestConfig {
    /**
     * 第一页（有的为0有的为1）
     */
    public static final int FIRST_PAGE = 1;

    public static final int RESPONSE_CODE_SUCCESS = 200;
    public static final int REQUEST_CODE_SUCCESS = 200;
    public static final int REQUEST_CODE_TOKEN_INVALID = 401;
    /**
     * 测试环境
     */
    public static final String SERVICE_IP = "192.168.0.3";
    public static final String SERVICE_PORT = ":8001";
    public static final String BASE_TEST_URL = "http://" + SERVICE_IP + SERVICE_PORT + "/";
    public static final String SOCKET_TEST_URL_IP = "ws://" + SERVICE_IP + SERVICE_PORT + "/webSocket/android:";
    /**
     * 线上环境
     */
    public static final String BASE_KONG_AN_URL = "https://dji.tklvyou.cn/";
    //    SERVICE_IP
    public static final String SOCKET_KONG_AN_URL_IP = "wss://dji.tklvyou.cn/"+  "webapi/webSocket/android:";

    /**
     * 检察院线上
     */
    public static final String SERVICE_PRO_IP = "1.13.4.164";
    //    SERVICE_IP
    public static final String SERVICE_PRO_PORT = ":8000";
    public static final String BASE_PRO_URL = "http://" + SERVICE_PRO_IP + SERVICE_PRO_PORT + "/";
    public static final String SOCKET_PRO_URL_IP = "ws://" + SERVICE_PRO_IP + SERVICE_PRO_PORT + "/webSocket/android:";

    /**
     * SAS
     */
    public static final String SERVICE__SAS_IP = "192.168.0.224";
    //    SERVICE_IP
    public static final String SERVICE_SAS_PORT = ":8760";
    public static final String BASE_SAS_URL = "http://" + SERVICE__SAS_IP + SERVICE_SAS_PORT + "/";
    public static final String SOCKET_SAS_URL_IP = "ws://" + SERVICE__SAS_IP + SERVICE_SAS_PORT + "/webSocket/android:";


}
