package com.tech.eskool;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * One shared OkHttp client for all API calls.
 *
 * The hosting firewall sometimes answers with a "Please wait while your request is being
 * verified..." browser check instead of JSON. {@link VerifyActivity} lets a WebView pass that
 * check; this client then re-uses the WebView's cookies and User-Agent so the server treats
 * the app's API requests as the same, already-verified visitor.
 */
public final class HttpClientProvider {

    private static final String TAG = "HttpClientProvider";
    private static volatile OkHttpClient client;

    private HttpClientProvider() { }

    /** Browser User-Agent; filled from the WebView as soon as a Context is available. */
    private static volatile String userAgent;

    /** Call once early (StartActivity / LoginActivity) so the WebView User-Agent is known. */
    public static OkHttpClient get(Context context) {
        if (userAgent == null && context != null) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context.getApplicationContext());
            } catch (Exception e) {
                Log.w(TAG, "Could not read WebView User-Agent", e);
            }
        }
        return get();
    }

    /** For places without a Context, e.g. ApiClient. */
    public static OkHttpClient get() {
        if (client == null) {
            synchronized (HttpClientProvider.class) {
                if (client == null) {
                    client = build();
                }
            }
        }
        return client;
    }

    private static OkHttpClient build() {
        return new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieJar())
                .addInterceptor(chain -> {
                    String ua = userAgent != null ? userAgent
                            : System.getProperty("http.agent", "Android");
                    return chain.proceed(chain.request().newBuilder()
                            .header("User-Agent", ua)
                            .build());
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /** Shares cookies between OkHttp and the WebView's CookieManager. */
    private static final class WebViewCookieJar implements CookieJar {

        @Override
        public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
            try {
                CookieManager cm = CookieManager.getInstance();
                for (Cookie cookie : cookies) {
                    cm.setCookie(url.toString(), cookie.toString());
                }
                cm.flush();
            } catch (Exception e) {
                Log.w(TAG, "Could not save cookies", e);
            }
        }

        @NonNull
        @Override
        public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
            List<Cookie> result = new ArrayList<>();
            try {
                String header = CookieManager.getInstance().getCookie(url.toString());
                if (header != null && !header.isEmpty()) {
                    for (String part : header.split(";")) {
                        Cookie c = Cookie.parse(url, part.trim());
                        if (c != null) result.add(c);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not load cookies", e);
            }
            return result;
        }
    }
}
