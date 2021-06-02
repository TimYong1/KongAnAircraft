package com.tourcoo.util.cache;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年06月02日16:36
 * @Email: 971613168@qq.com
 */
public interface ExecuteListener {
    void onExecuteStart();
    void onExecuteFinish(String result);
    void onError(Throwable e);
}
