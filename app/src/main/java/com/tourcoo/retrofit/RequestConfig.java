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
   /* public static final String SERVICE_IP = "192.168.0.3";
    public static final String SERVICE_PORT = ":8001";
    public static final String BASE_URL = "http://" + SERVICE_IP + SERVICE_PORT + "/";
    public static final String SOCKET_URL_IP = "ws://" + SERVICE_IP + SERVICE_PORT + "/webSocket/android:";*/
    /**
     * 线上环境
     */
  /*  public static final String BASE_URL = "https://dji.tklvyou.cn/";
    //    SERVICE_IP
    public static final String SOCKET_URL_IP = "wss://dji.tklvyou.cn/"+  "webapi/webSocket/android:";*/

    /**
     * 检察院线上
     */
    public static final String SERVICE_IP = "1.13.4.164";
    //    SERVICE_IP
    public static final String SERVICE_PORT = ":8000";
    public static final String BASE_URL = "http://" + SERVICE_IP + SERVICE_PORT + "/";
    public static final String SOCKET_URL_IP = "ws://" + SERVICE_IP + SERVICE_PORT + "/webSocket/android:";




}
