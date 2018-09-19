package com.batmobi.util;

import android.util.Log;

/**
 * description:
 * author: kyXiao
 * created date: 2018/9/12
 */

public class LogUtil {

    private static boolean DEBUG = true;

    public static void out(String TAG, String msg) {
        if (DEBUG)
            Log.i(TAG, msg);
    }

    public static void error(String TAG, String msg) {
        if (DEBUG)
            Log.e(TAG, msg);
    }
}
