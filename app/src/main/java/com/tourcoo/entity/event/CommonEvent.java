package com.tourcoo.entity.event;

/**
 * @author :JenkinsZhou
 * @description :普通公共事件
 * @company :途酷科技
 * @date 2021年05月07日11:28
 * @Email: 971613168@qq.com
 */
public class CommonEvent {
    private int code;
    private String action;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public CommonEvent(int code, String action) {
        this.code = code;
        this.action = action;
    }

    public CommonEvent() {
    }

    public CommonEvent(String action) {
        this.action = action;
    }
}
