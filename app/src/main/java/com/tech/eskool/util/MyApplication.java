package com.tech.eskool.util;

import android.app.Application;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.tech.eskool.HttpClientProvider;
import com.tech.eskool.Log;
import com.tech.eskool.notification.MyFirebaseMessagingService;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AppLifecycleObserver());
        MyFirebaseMessagingService.ensureChannel(this);
        setUpPicasso();
    }

    /**
     * The server sends menu icons without Cache-Control, so the HTTP cache guessed a lifetime of
     * weeks and kept showing old icons after they were replaced on the server. Images are now
     * re-checked with the server on every load (a cheap "304 Not Modified" when unchanged), and
     * the old cache, full of stale icons, is deleted.
     */
    private void setUpPicasso() {
        deleteRecursively(new File(getCacheDir(), "picasso-cache")); // Picasso's old default cache
        OkHttpClient client = HttpClientProvider.get(this).newBuilder()
                .cache(new Cache(new File(getCacheDir(), "image-cache"), 50L * 1024 * 1024))
                .addNetworkInterceptor(chain -> {
                    okhttp3.Response response = chain.proceed(chain.request());
                    if (response.header("Cache-Control") == null) {
                        return response.newBuilder().header("Cache-Control", "no-cache").build();
                    }
                    return response;
                })
                .build();
        try {
            Picasso.setSingletonInstance(new Picasso.Builder(this)
                    .downloader(new OkHttp3Downloader(client))
                    .build());
        } catch (IllegalStateException e) {
            Log.w("MyApplication", "Picasso already set up", e);
        }
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) deleteRecursively(child);
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
