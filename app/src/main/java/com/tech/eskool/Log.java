package com.tech.eskool;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Drop-in replacement for android.util.Log that sends log lines to the server
 * (api/app-log.php -> android-app.log) instead of keeping them on the phone.
 *
 * Use it exactly like android.util.Log: just import com.tech.eskool.Log instead.
 *  - Release builds: nothing is written to Logcat on the user's phone.
 *  - Debug builds:   lines also go to Logcat, so development is unchanged.
 * Lines are batched and sent every 15 s; warnings and errors are sent right away.
 * Never log passwords, OTPs or tokens with this class - everything ends up on the server.
 */
public final class Log {

    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int DEBUG = android.util.Log.DEBUG;
    public static final int INFO = android.util.Log.INFO;
    public static final int WARN = android.util.Log.WARN;
    public static final int ERROR = android.util.Log.ERROR;
    public static final int ASSERT = android.util.Log.ASSERT;

    /** Must match LOG_KEY in api/app-log.php. Only stops casual spam; it is not a secret. */
    private static final String LOG_KEY = "871ac077075c3c393ef71b08529d7366";
    private static final String ENDPOINT = "app-log.php";

    private static final int MAX_QUEUE = 500;      // oldest lines are dropped beyond this
    private static final int MAX_BATCH = 100;      // lines per request
    private static final int MAX_MESSAGE = 4000;   // chars per line
    private static final long FLUSH_SECONDS = 15;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String SESSION = UUID.randomUUID().toString().substring(0, 8);

    private static final ConcurrentLinkedQueue<JSONObject> QUEUE = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger SIZE = new AtomicInteger();
    private static volatile String user = "";

    private static final ScheduledExecutorService SENDER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "remote-log");
                t.setDaemon(true);
                return t;
            });

    static {
        SENDER.scheduleWithFixedDelay(Log::flushSafely, FLUSH_SECONDS, FLUSH_SECONDS, TimeUnit.SECONDS);
    }

    private Log() { }

    /** Optional: who is using the app (e.g. "53495/EST39"), added to every line. */
    public static void setUser(String who) {
        user = who == null ? "" : who;
    }

    // ---- same API as android.util.Log -------------------------------------------------

    public static int v(String tag, String msg) { return log(VERBOSE, tag, msg, null); }
    public static int v(String tag, String msg, Throwable tr) { return log(VERBOSE, tag, msg, tr); }
    public static int d(String tag, String msg) { return log(DEBUG, tag, msg, null); }
    public static int d(String tag, String msg, Throwable tr) { return log(DEBUG, tag, msg, tr); }
    public static int i(String tag, String msg) { return log(INFO, tag, msg, null); }
    public static int i(String tag, String msg, Throwable tr) { return log(INFO, tag, msg, tr); }
    public static int w(String tag, String msg) { return log(WARN, tag, msg, null); }
    public static int w(String tag, String msg, Throwable tr) { return log(WARN, tag, msg, tr); }
    public static int w(String tag, Throwable tr) { return log(WARN, tag, "", tr); }
    public static int e(String tag, String msg) { return log(ERROR, tag, msg, null); }
    public static int e(String tag, String msg, Throwable tr) { return log(ERROR, tag, msg, tr); }
    public static int wtf(String tag, String msg) { return log(ASSERT, tag, msg, null); }
    public static int wtf(String tag, String msg, Throwable tr) { return log(ASSERT, tag, msg, tr); }
    public static int wtf(String tag, Throwable tr) { return log(ASSERT, tag, "", tr); }
    public static int println(int priority, String tag, String msg) { return log(priority, tag, msg, null); }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static boolean isLoggable(String tag, int level) {
        return true;
    }

    // ---- internals --------------------------------------------------------------------

    private static int log(int priority, String tag, String msg, Throwable tr) {
        String text = msg == null ? "" : msg;
        if (tr != null) text = text + "\n" + getStackTraceString(tr);

        if (BuildConfig.DEBUG) {
            android.util.Log.println(priority, tag == null ? "" : tag, text);
        }

        try {
            if (text.length() > MAX_MESSAGE) text = text.substring(0, MAX_MESSAGE) + "...[cut]";
            JSONObject line = new JSONObject();
            line.put("t", System.currentTimeMillis());
            line.put("l", levelName(priority));
            line.put("tag", tag == null ? "" : tag);
            line.put("m", text);
            line.put("th", Thread.currentThread().getName());
            QUEUE.add(line);
            if (SIZE.incrementAndGet() > MAX_QUEUE && QUEUE.poll() != null) SIZE.decrementAndGet();
            if (priority >= WARN) SENDER.execute(Log::flushSafely);
        } catch (Throwable ignored) {
            // logging must never crash the app
        }
        return 0;
    }

    private static void flushSafely() {
        try {
            flush();
        } catch (Throwable t) {
            if (BuildConfig.DEBUG) android.util.Log.w("RemoteLog", "send failed", t);
        }
    }

    /** Runs on the single "remote-log" thread only. */
    private static void flush() throws Exception {
        while (!QUEUE.isEmpty()) {
            List<JSONObject> batch = new ArrayList<>();
            JSONObject line;
            while (batch.size() < MAX_BATCH && (line = QUEUE.poll()) != null) {
                SIZE.decrementAndGet();
                batch.add(line);
            }
            if (batch.isEmpty()) return;

            JSONObject device = new JSONObject();
            device.put("app", BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
            device.put("build", BuildConfig.DEBUG ? "debug" : "release");
            device.put("model", Build.MANUFACTURER + " " + Build.MODEL);
            device.put("android", Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")");
            device.put("session", SESSION);
            device.put("user", user);

            JSONObject body = new JSONObject();
            body.put("device", device);
            body.put("logs", new JSONArray(batch));

            HttpUrl url = com.tech.eskool.rest_service.ApiClient.getClient().baseUrl().resolve(ENDPOINT);
            if (url == null) return;
            Request request = new Request.Builder()
                    .url(url)
                    .header("X-Log-Key", LOG_KEY)
                    .post(RequestBody.create(JSON, body.toString()))
                    .build();

            try (Response response = HttpClientProvider.get().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    // Server refused (e.g. firewall page) - drop this batch, don't retry forever.
                    if (BuildConfig.DEBUG) {
                        android.util.Log.w("RemoteLog", "server replied HTTP " + response.code());
                    }
                    return;
                }
            }
        }
    }

    private static String levelName(int priority) {
        switch (priority) {
            case VERBOSE: return "V";
            case DEBUG: return "D";
            case INFO: return "I";
            case WARN: return "W";
            case ERROR: return "E";
            default: return "A";
        }
    }
}
