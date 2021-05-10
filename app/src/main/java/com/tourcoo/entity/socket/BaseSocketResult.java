package com.tourcoo.entity.socket;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年03月16日11:44
 * @Email: 971613168@qq.com
 */
public class BaseSocketResult<T> {
    private T data;
    private int msgType;
    private String webId;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getWebId() {
        return webId;
    }

    public void setWebId(String webId) {
        this.webId = webId;
    }
}
