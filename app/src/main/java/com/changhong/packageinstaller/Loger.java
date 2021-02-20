package com.changhong.packageinstaller;

import android.util.Log;

/**
 * 作者:
 * 创建日期:
 * 类说明:
 **/
public class Loger {
    private static final String TAG = "installer";

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }
}
