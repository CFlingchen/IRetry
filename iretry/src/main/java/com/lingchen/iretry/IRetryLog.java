package com.lingchen.iretry;

import android.util.Log;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function  日志
 */

public class IRetryLog {
    private static final String TAG = "IRetry";
    static boolean debug;

    public static void e(String msg) {
        if (debug)
            Log.e(TAG, msg);
    }

    public static void d(String msg) {
        if (debug)
            Log.d(TAG, msg);
    }

    public static void i(String msg) {
        if (debug)
            Log.i(TAG, msg);
    }
}
