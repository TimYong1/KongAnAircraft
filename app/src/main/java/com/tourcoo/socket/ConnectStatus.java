package com.tourcoo.socket;

/**
 * @author :JenkinsZhou
 * @description : 连接状态
 * @company :途酷科技
 * @date 2021年03月15日15:04
 * @Email: 971613168@qq.com
 */
public enum ConnectStatus {

    Connecting, // the initial state of each web socket.
    Open, // the web socket has been accepted by the remote peer
    Closing, // one of the peers on the web socket has initiated a graceful shutdown
    Closed, //  the web socket has transmitted all of its messages and has received all messages from the peer
    Canceled // the web socket connection failed
}
