package com.tech.eskool;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Opens the server in a WebView so its "request is being verified" browser check can run
 * (and the user can tap it if it asks). When the check is passed, the verification cookie
 * is stored in CookieManager, which {@link HttpClientProvider} then sends with API requests.
 *
 * Returns RESULT_OK when the check page is gone, RESULT_CANCELED on timeout or back press.
 */
public class VerifyActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "url";

    private static final long POLL_MS = 1500;
    private static final long TIMEOUT_MS = 45000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView webView;
    private long startedAt;
    private boolean done;

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            if (done) return;
            if (System.currentTimeMillis() - startedAt > TIMEOUT_MS) {
                finishWith(RESULT_CANCELED);
                return;
            }
            checkPage();
            handler.postDelayed(this, POLL_MS);
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        root.setFitsSystemWindows(true);

        TextView header = new TextView(this);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics());
        header.setPadding(pad, pad, pad, pad);
        header.setGravity(Gravity.CENTER);
        header.setText("Verifying your connection…\nThis only takes a moment.");
        header.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWith(RESULT_CANCELED);
            }
        });

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                checkPage();
            }
        });

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null) {
            finishWith(RESULT_CANCELED);
            return;
        }
        startedAt = System.currentTimeMillis();
        webView.loadUrl(url);
        handler.postDelayed(poll, POLL_MS);
    }

    /** The check is passed once the page no longer shows the verification text. */
    private void checkPage() {
        if (done || webView == null) return;
        webView.evaluateJavascript(
                "(function(){return document.body ? document.body.innerText : '';})()",
                value -> {
                    if (done || value == null) return;
                    String text = value.toLowerCase();
                    boolean stillChecking = text.contains("being verified")
                            || text.contains("one moment")
                            || text.contains("just a moment")
                            || text.contains("verify you are human")
                            || text.contains("checking your browser");
                    boolean loaded = text.length() > 2; // "" means page still blank
                    if (loaded && !stillChecking) {
                        finishWith(RESULT_OK);
                    }
                });
    }

    private void finishWith(int result) {
        if (done) return;
        done = true;
        handler.removeCallbacksAndMessages(null);
        CookieManager.getInstance().flush();
        setResult(result);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
