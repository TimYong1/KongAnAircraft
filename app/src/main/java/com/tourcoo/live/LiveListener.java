package com.tourcoo.live;

/**
 * @author :JenkinsZhou
 * @description :直播状态监听
 * @company :途酷科技
 * @date 2021年06月09日15:17
 * @Email: 971613168@qq.com
 */
public interface LiveListener {
    void liveOpenResult(int resultCode);
    void liveStop();
}
