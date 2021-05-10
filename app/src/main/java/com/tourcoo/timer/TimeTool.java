package com.tourcoo.timer;

import android.os.Handler;
import android.os.Looper;

import com.apkfuns.logutils.LogUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * author  xiandanin
 * created 2017/5/16 11:32
 */
public class TimeTool implements ITimerSupport {
    public static final String TAG = "TimeTool计时器";
    private Timer mTimer;

    private Handler mHandler;

    /**
     * 倒计时时间
     */
    private long mMillisInFuture;

    /**
     * 间隔时间
     */
    private long mCountDownInterval;
    /**
     * 倒计时剩余时间
     */
    private long mMillisUntilFinished;

    private OnCountDownTimerListener mOnCountDownTimerListener;

    private TimerState mTimerState = TimerState.FINISH;

    @Deprecated
    public TimeTool() {
        this.mHandler = new Handler();
    }


    public TimeTool(long millisInFuture, long countDownInterval) {
        this.setMillisInFuture(millisInFuture);
        this.setCountDownInterval(countDownInterval);
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * @param millisInFuture
     * @deprecated 使用构造方法
     */
    @Deprecated
    public void setMillisInFuture(long millisInFuture) {
        this.mMillisInFuture = millisInFuture;
        this.mMillisUntilFinished = mMillisInFuture;
    }

    @Override
    public void start() {
        //防止重复启动 重新启动要先reset再start
        if (mTimer == null && mTimerState != TimerState.START) {
            mTimer = new Timer();
            try {
                mTimer.scheduleAtFixedRate(createTimerTask(), mCountDownInterval, mCountDownInterval);
                mTimerState = TimerState.START;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                LogUtils.e(TAG + "计时器启动异常" + e.toString());
            }

        } else {
            LogUtils.e("计时器状态异常 无法start");
        }
    }

    @Override
    public void pause() {
        if (mTimer != null && mTimerState == TimerState.START) {
            cancelTimer();
            mTimerState = TimerState.PAUSE;
        }
    }

    @Override
    public void resume() {
        if (mTimerState == TimerState.PAUSE) {
            start();
        }
    }

    @Override
    public void stop() {
        stopTimer(true);
    }

    @Override
    public void reset() {
        if (mTimer != null) {
            cancelTimer();
        }
        mMillisUntilFinished = mMillisInFuture;
        mTimerState = TimerState.FINISH;
        mTimer = null;
    }

    private void stopTimer(final boolean cancel) {
        if (mTimer != null) {
            cancelTimer();
            mMillisUntilFinished = mMillisInFuture;
            mTimerState = TimerState.FINISH;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnCountDownTimerListener != null) {
                        if (cancel) {
                            mOnCountDownTimerListener.onCancel();
                        } else {
                            mOnCountDownTimerListener.onFinish();
                        }
                    }
                }
            });
        }
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            if (mTimer != null) {
                mTimer.purge();
            }
        }
        mTimer = null;
    }

    public boolean isStart() {
        return mTimerState == TimerState.START;
    }

    public boolean isFinish() {
        return mTimerState == TimerState.FINISH;
    }


    /**
     * @param countDownInterval
     * @deprecated 使用构造方法
     */
    @Deprecated
    public void setCountDownInterval(long countDownInterval) {
        this.mCountDownInterval = countDownInterval;
    }

    public void setOnCountDownTimerListener(OnCountDownTimerListener listener) {
        this.mOnCountDownTimerListener = listener;
    }

    public long getMillisUntilFinished() {
        return mMillisUntilFinished;
    }

    public TimerState getTimerState() {
        return mTimerState;
    }

    protected TimerTask createTimerTask() {
        return new TimerTask() {
            private long startTime = -1;

            @Override
            public void run() {
                if (startTime < 0) {
                    //第一次回调 记录开始时间

                    startTime = scheduledExecutionTime() - (mMillisInFuture - mMillisUntilFinished);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnCountDownTimerListener != null) {
                                mOnCountDownTimerListener.onTick(mMillisUntilFinished);
                            }
                        }
                    });
                } else {
                    //剩余时间
                    mMillisUntilFinished = mMillisInFuture - (scheduledExecutionTime() - startTime);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnCountDownTimerListener != null) {
                                mOnCountDownTimerListener.onTick(mMillisUntilFinished);
                            }
                        }
                    });
                    if (mMillisUntilFinished <= 0) {
                        //如果没有剩余时间 就停止
                        stopTimer(false);
                    }
                }
            }
        };
    }

}
