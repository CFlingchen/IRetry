package com.lingchen.iretrydemo;

import android.app.Application;

import com.lingchen.iretry.IRetry;

/**
 * Author    lingchen
 * Email     838878458@qq.com
 * Time      2018/9/19
 * Function
 */

public class App extends Application {
    static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //开启测试用例
        IRetry.enableDebug(true);
    }

    public static App getApp() {
        return app;
    }
}
