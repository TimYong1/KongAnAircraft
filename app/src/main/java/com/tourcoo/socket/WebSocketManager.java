package com.tourcoo.socket;


import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.constant.AccountConstant;
import com.tourcoo.timer.OnCountDownTimerListener;
import com.tourcoo.timer.TimeTool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @author :JenkinsZhou
 * @description : Socket封装
 * @company :途酷科技
 * @date 2021年03月15日15:03
 * @Email: 971613168@qq.com
 */
public final class WebSocketManager extends WebSocketListener {
    private static final String TAG = "WebSocketManager ";
    public static final String RESPONSE_PANG = "pong";
    private TimeTool pingTimer;
    private TimeTool pongTimer;
    private TimeTool connectTimer;
    private static final int DISCONNECTED_TIME = 2;
    private int userOffLineCount = 0;
    private StringBuilder stringBuilder = new StringBuilder();
    /**
     * 是否在传输数据中
     */
    private boolean isTransmission = false;

    /**
     * ping的间隔时间单位：秒
     */
    public int pingTimeIntervalSecond = 20;


    /**
     * pang的间隔时间单位：秒
     */
    public int pangTimeoutSecond = 5;

    private String wsUrl;

    private WebSocket webSocket;

    private ConnectStatus status;

    private OkHttpClient client = new OkHttpClient.Builder()
            .build();

    private WebSocketManager(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    private static WebSocketManager INST;

    public static WebSocketManager getInstance(String url) {
        if (INST == null) {
            synchronized (WebSocketManager.class) {
                INST = new WebSocketManager(url);
            }
        }
        return INST;
    }

    public ConnectStatus getStatus() {
        return status;
    }

    public void connect() {
        //构造request对象
        Request request = new Request.Builder()
                .url(wsUrl)
                .build();
        webSocket = client.newWebSocket(request, this);
        status = ConnectStatus.Connecting;


    }

    public void reConnect() {
        if (webSocket != null) {
            webSocket = client.newWebSocket(webSocket.request(), this);
            LogUtils.i(TAG + "正在重连...");
        }
    }

    public boolean send(String text) {
        if (webSocket != null && getStatus() == ConnectStatus.Open) {
            isTransmission = true;
            return webSocket.send(text);
        }
        return false;
    }

    public void cancel() {
        if (webSocket != null) {
            webSocket.cancel();
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        this.status = ConnectStatus.Open;
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onOpen();
        }
        if (userOffLineCount > 0) {
            LogUtils.i(TAG + "重接成功");
            logString("重接成功");
        } else {
            LogUtils.i(TAG + "连接成功");
            logString("连接成功：" + webSocket.toString());
        }
        userOffLineCount = 0;
        releaseConnectTimer();
        startOrResetPingTimerTask();

    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onMessage(text);
        }
        if (RESPONSE_PANG.equalsIgnoreCase(text)) {
            //表示收到服务器返回的心跳应答
            LogUtils.i(TAG + "---收到心跳应答----");
            isTransmission = false;
            stopTimerPong();
            return;
        }
        isTransmission = true;
        //如果是传输数据 则需要重置心跳包计时器并重新计时
        LogUtils.i("检测到服务器发回了数据：" + text);
        startOrResetPingTimerTask();
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        this.status = ConnectStatus.Closing;
//        log("onClosing");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
//        log("onClosed");
        this.status = ConnectStatus.Closed;
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onClosed();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        LogUtils.e(TAG + "WebSocket : onFailure" + t.toString());
        logString("socket-onFailure=" + t.toString());
        t.printStackTrace();
        this.status = ConnectStatus.Canceled;
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onConnectError(t);
        }
        //重连的时候先把心跳包发送逻辑取消
        stopTimerPing();
        doReconnectedDelay();
    }


    private WebSocketCallBack mSocketIOCallBack;

    public void setSocketIOCallBack(WebSocketCallBack callBack) {
        mSocketIOCallBack = callBack;
    }

    public void removeSocketIOCallBack() {
        mSocketIOCallBack = null;
    }

    private void sendPing() {
        isTransmission = false;
        if (webSocket != null && ConnectStatus.Open == status) {
            webSocket.send("ping");
            LogUtils.i(TAG + "心跳包发送中...");
            startPongTimerTask();
        } else {
            userOffLineCount++;
            logString("webSocket连接异常 发送心跳包已经失败 " + userOffLineCount + "次");
            LogUtils.e(TAG + "webSocket连接异常 发送心跳包已经失败 " + userOffLineCount + "次");
            if (userOffLineCount > DISCONNECTED_TIME) {
//                doReconnectedDelay();
                //心跳包接收失败后 重新建立连接
                doNewConnectedDelay();
            }
        }
    }


    private void startOrResetPingTimerTask() {
        isTransmission = false;
        if (pingTimer == null) {
            pingTimer = createPingTimer();
        } else {
            pingTimer.reset();
            LogUtils.i(TAG + "pingTimer已重置");
        }
        pingTimer.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isTransmission) {
                    LogUtils.d(TAG + "当前正处于数据传输状态，无需发送心跳包");
                    return;
                }
                sendPing();
            }

            @Override
            public void onFinish() {
                LogUtils.i(TAG + "计时结束 重新开始计时");
                startOrResetPingTimerTask();
            }

            @Override
            public void onCancel() {
                LogUtils.w(TAG + "pingTimer计时器取消");
            }
        });
        pingTimer.start();
    }


    private TimeTool createPingTimer() {
        return new TimeTool(Integer.MAX_VALUE, pingTimeIntervalSecond * 1000);
    }

    /**
     * 服务器响应计时器
     *
     * @return
     */
    private TimeTool createPongTimer() {
        if (pangTimeoutSecond <= 0) {
            pangTimeoutSecond = 10;
        }
        return new TimeTool(pangTimeoutSecond * 1000, 1000);
    }

    private TimeTool getReConnectTimer() {
        return new TimeTool(5 * 1000, 1000);
    }


    private void startPongTimerTask() {
        if (pongTimer == null) {
            pongTimer = createPongTimer();
        } else {
            pongTimer.reset();
        }
        pongTimer.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                //如果计时完成 说明服务器响应时间已经超时了 此时视为连接断开 需要重新连接
                if (webSocket != null) {
                    LogUtils.d(TAG + "服务器响应超时,正在重连");
                    logString("服务器响应超时,正在重连");
                    webSocket.cancel();
                    reConnect();
                } else {
                    connect();
                    logString("服务器响应超时,webSocket == null需要建立新连接");
                    LogUtils.i(TAG + "服务器响应超时,webSocket == null需要建立新连接");
                }
            }

            @Override
            public void onCancel() {
                LogUtils.d("本次pongTimer计时器取消");
            }
        });
        pongTimer.start();
    }


    private void stopTimerPong() {
        if (pongTimer != null) {
            pongTimer.stop();
        }
    }

    private void stopTimerPing() {
        if (pingTimer != null) {
            pingTimer.stop();
        }
    }

    private void doReconnectedDelay() {
        LogUtils.d(TAG + "5秒后开始重连");
        logString("5秒后开始重连");
        if (connectTimer == null) {
            connectTimer = getReConnectTimer();
        }
        connectTimer.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                reConnect();
            }

            @Override
            public void onCancel() {
                LogUtils.d(TAG + "doReconnectedDelay（）执行了onCancel");
            }
        });
        connectTimer.start();
    }

    private void doNewConnectedDelay() {
        cancel();
        close();
        webSocket = null;
        LogUtils.d(TAG + "先将原有的连接取消并关闭并在5秒后开始建立新连接");
        logString("先将原有的连接取消并关闭并在5秒后开始建立新连接");
        if (connectTimer == null) {
            connectTimer = getReConnectTimer();
        }
        connectTimer.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                connect();
            }

            @Override
            public void onFinish() {
                reConnect();
            }

            @Override
            public void onCancel() {
                LogUtils.d(TAG + "doNewConnectedDelay（）执行了onCancel");
            }
        });
        connectTimer.start();
    }


    private void releaseConnectTimer() {
        if (connectTimer != null) {
            LogUtils.d(TAG + "取消connectTimer计时器");
            connectTimer.stop();
            connectTimer = null;
        }
    }


    public void release() {
        if (pingTimer != null) {
            pingTimer.stop();
            pingTimer = null;
        }
        if (pongTimer != null) {
            pongTimer.stop();
            pongTimer = null;
        }
        releaseConnectTimer();
        cancel();
        close();
        removeSocketIOCallBack();
        webSocket = null;
        INST = null;
    }

    private String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault());
        return format.format(new Date());
    }

    private void logString(String str) {
        AccountConstant.testStr = stringBuilder.append("\n").append(getTime()).append(str).toString();
    }
}
