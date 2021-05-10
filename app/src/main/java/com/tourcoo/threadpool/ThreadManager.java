package com.tourcoo.threadpool;

/**
 * @author :JenkinsZhou
 * @description : 线程管理类
 * @company :途酷科技
 * @date 2021年03月11日13:40
 * @Email: 971613168@qq.com
 */
public class ThreadManager {

    private final static ThreadPool io;
    private final static ThreadPool cache;
    private final static ThreadPool calculator;
    private final static ThreadPool file;

    public static ThreadPool getIO () {
        return io;
    }

    public static ThreadPool getCache() {
        return cache;
    }
    public static ThreadPool getDefault() {
        return cache;
    }

    public static ThreadPool getCalculator() {
        return calculator;
    }

    public static ThreadPool getFile() {
        return file;
    }

    static {
        io = ThreadPool.Builder.createFixed(6).setName("IO").setPriority(7).setCallback(new DefaultCallback()).build();
        cache = ThreadPool.Builder.createCacheable().setName("cache").setCallback(new DefaultCallback()).build();
        calculator = ThreadPool.Builder.createFixed(4).setName("calculator").setPriority(Thread.MAX_PRIORITY).setCallback(new DefaultCallback()).build();
        file = ThreadPool.Builder.createFixed(4).setName("file").setPriority(3).setCallback(new DefaultCallback()).build();
    }

    private static class DefaultCallback implements ThreadCallback {

        @Override
        public void onError(String threadName, Throwable t) {
//            Log.e("Task with thread %s has occurs an error: %s", threadName, t.getMessage());
        }

        @Override
        public void onCompleted(String threadName) {
//            MyLog.d("Task with thread %s completed", threadName);
        }

        @Override
        public void onStart(String threadName) {
//            MyLog.d("Task with thread %s start running!", threadName);
        }
    }
}
