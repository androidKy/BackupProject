package com.batmobi.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * description:线程工具类，支持异步及主线程
 * author: diff
 * date: 2018/4/26.
 */
public class ThreadUtil {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static ExecutorService exe = Executors.newCachedThreadPool();

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void checkRunOnAsyncThread() {
        if (isMainThread()) {
            throw new IllegalStateException("can't do this on main thread!");
        }
    }

    public static void checkRunOnMainThread() {
        if (!isMainThread()) {
            throw new IllegalStateException("must do this on main thread!");
        }
    }

    public static void main(Runnable runnable) {
        main(runnable, 0);
    }

    public static void main(Runnable runnable, long delay) {
        try {
            if (delay <= 0) {
                handler.post(runnable);
            } else {
                handler.postDelayed(runnable, delay);
            }
        } catch (Throwable tw) {
            tw.printStackTrace();
        }

    }

    public static void async(Runnable runnable) {
        async(runnable, 0);
    }

    public static void async(final Runnable runnable, long delay) {
        try {
            if (delay <= 0) {
                exe.execute(runnable);
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exe.execute(runnable);
                    }
                }, delay);
            }
        } catch (Throwable tw) {
            tw.printStackTrace();
        }
    }
}
