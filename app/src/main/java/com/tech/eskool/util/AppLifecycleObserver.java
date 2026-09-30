package com.tech.eskool.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppLifecycleObserver implements Application.ActivityLifecycleCallbacks {

    private static boolean isInForeground = false;

    public static boolean isInForeground() {
        return isInForeground;
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        isInForeground = true;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        isInForeground = true;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        isInForeground = true;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        isInForeground = false;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        isInForeground = false;
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        isInForeground = false;
    }
}
