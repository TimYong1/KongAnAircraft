package com.tourcoo.entity;

/**
 * @author :JenkinsZhou
 * @description : 网络请求基类
 * @company :途酷科技
 * @date 2020年11月13日16:21
 * @Email: 971613168@qq.com
 */
public class BaseResultOld<T> {
    /**
     * code : 1
     * errMsg : 操作成功
     * data : {}
     */

    public int status;
    public String message;
    public T data;


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
