package com.tourcoo.util;

import android.content.Context;
import android.os.Vibrator;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年04月07日14:30
 * @Email: 971613168@qq.com
 */
public class VibratorPlayer {
    private Vibrator vibrator;

    public VibratorPlayer(Context context) {
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * 开始震动
     *
     * @param delay    延时时间
     * @param interval 震动间隔
     * @param isRepeat 是否重复
     */
    public void play(long delay, long interval, boolean isRepeat) {
        long[] pattern = {delay, interval};
        this.vibrator.vibrate(pattern, isRepeat ? 0 : -1);
    }

    /**
     * 停止震动
     */
    public void stop() {
        this.vibrator.cancel();
    }


}
