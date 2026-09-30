package com.tech.eskool.ui.home;

import static com.tech.eskool.StartActivity.NO_INTERNET;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.tech.eskool.R;
import com.tech.eskool.service.SessionManager;

public class HomeFragment extends Fragment {

    WebView webView;
    Context context;
    SessionManager sessionManager;
    ProgressBar progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        sessionManager = new SessionManager(context);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initialization(root);
        return root;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initialization(View v) {

        webView = v.findViewById(R.id.web_view);
        progressBar = v.findViewById(R.id.progressBar);
        webView.setWebViewClient(new MywebViewClient());
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setPadding(0, 0, 0, 0);
        webView.addJavascriptInterface(new JavascriptInterface[]{}, "Android");
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                //try-catch for downloading...
            }
        });

    }

    public void getAlertWithOkForActivity(String msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.myAlertDialog);
        alert.setTitle(R.string.app_name);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (webView != null && sessionManager.checkConnectivity(context)) {
            String token = sessionManager.getStudentToken() != null ?
                    sessionManager.getStudentToken() : sessionManager.getUserToken();
            String webUrl = sessionManager.getLandingUrl() + "?college_code=" + sessionManager.getCollegeCode() + "&session_id=" + sessionManager.getSessionId() + "&token=" + token;
            webView.loadUrl(webUrl);
        } else {
            getAlertWithOkForActivity(NO_INTERNET);
        }
    }


    class MywebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            webView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            webView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}