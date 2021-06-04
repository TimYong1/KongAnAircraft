package com.tourcoo.entity.base;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年06月03日12:00
 * @Email: 971613168@qq.com
 */
public class BaseSasResult<T> extends BaseResult<T> {
    private int code;
    private String msg;
    private String errorMsg;
    private String path;
    private String extra;
    private String timestamp;
    public T data;
    private boolean isSuccess;
    @Override
    public int getStatus() {
        return code;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    @Override
    public T getData() {
        return data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
