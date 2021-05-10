package com.tourcoo.entity.event;

/**
 * @author :JenkinsZhou
 * @description : 注册事件
 * @company :途酷科技
 * @date 2021年04月22日10:40
 * @Email: 971613168@qq.com
 */
public class RegisterEvent {
    private int code;

    public RegisterEvent(int code) {
        this.code = code;
    }

    public RegisterEvent() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
