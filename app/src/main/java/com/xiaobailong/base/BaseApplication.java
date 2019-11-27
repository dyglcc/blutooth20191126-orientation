package com.xiaobailong.base;

import android.app.Application;

import com.xiaobailong.tools.CrashHandler;

/**
 * Created by dongyuangui on 2017/5/31.
 */

public class BaseApplication extends Application {
    public static BaseApplication app = null;
    private Thread.UncaughtExceptionHandler defaultHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        CrashHandler.getInstance().run(this);
    }

}
