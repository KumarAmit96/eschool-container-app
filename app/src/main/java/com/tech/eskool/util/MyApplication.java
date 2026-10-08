package com.tech.eskool.util;

import android.app.Application;

import com.tech.eskool.notification.MyFirebaseMessagingService;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AppLifecycleObserver());
        MyFirebaseMessagingService.ensureChannel(this);
    }
}
