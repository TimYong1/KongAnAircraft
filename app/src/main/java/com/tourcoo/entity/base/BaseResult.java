package com.tourcoo.entity.base;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年06月03日11:58
 * @Email: 971613168@qq.com
 */
public abstract class BaseResult<T> {
    public abstract int getStatus();

    public abstract String getMessage();

    public abstract T getData();
}
