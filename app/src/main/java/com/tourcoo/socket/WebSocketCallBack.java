package com.tourcoo.socket;

/**
 * @author :JenkinsZhou
 * @description : WebSocket回调接口
 * @company :途酷科技
 * @date 2021年03月15日15:07
 * @Email: 971613168@qq.com
 */
public interface WebSocketCallBack {

    void onOpen();

    void onMessage(String text);

    void onClosed();

    void onConnectError(Throwable throwable);
}
