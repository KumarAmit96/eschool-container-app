package com.tech.eskool.ui.item;

import static android.app.Activity.RESULT_OK;
import static com.tech.eskool.StartActivity.NO_INTERNET;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import com.tech.eskool.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.tech.eskool.BuildConfig;
import com.tech.eskool.R;
import com.tech.eskool.database.MyDatabase;
import com.tech.eskool.rest_service.ApiClient;
import com.tech.eskool.service.SessionManager;
import com.tech.eskool.util.JavaScriptInterface;

import java.util.List;
import java.util.Objects;


public class MenuFragment extends Fragment {

    WebView webView;
    Context context;
    SessionManager sessionManager;
    MyDatabase myDatabase;
    ProgressBar progressBar;
    Boolean isLoaded = true;
    ValueCallback<Uri[]> uploadMessage;
    ActivityResultLauncher<Intent> fileChooserLauncher;
    String _url;
    boolean clearHistoryOnLoad = false;
    boolean sessionExpiredHandled = false;
    // The website's own login form. The server sends the app there when the login token is no longer valid.
    private static final java.util.Set<String> WEB_LOGIN_PATHS = new java.util.HashSet<>(java.util.Arrays.asList(
            "", "/", "/index.php", "/index.html", "/login.php"));

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        sessionManager = new SessionManager(context);
        myDatabase = new MyDatabase(context, null, null, 0);
        fileChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (uploadMessage == null) return;
                    Uri[] results = null;
                    if (result.getResultCode() == RESULT_OK) {
                        results = WebChromeClient.FileChooserParams.parseResult(result.getResultCode(), result.getData());
                    }
                    // Must always be called (null = cancelled) or the page's file input stops working.
                    uploadMessage.onReceiveValue(results);
                    uploadMessage = null;
                }
        );

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initialization(root);
        return root;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // This callback outranks the activity's drawer handling, so close an open drawer first.
                DrawerLayout drawer = requireActivity().findViewById(R.id.drawer_layout);
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else if (webView != null && sessionManager.getMenuUrl() != null) {
                    showHome();   // Back from a menu page returns to Home
                } else {
                    sessionManager.exit();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initialization(View v) {
        if (sessionManager.checkConnectivity(context)) {
            webView = v.findViewById(R.id.web_view);
            progressBar = v.findViewById(R.id.progressBar);
            webView.setWebViewClient(new MywebViewClient());
            webView.setWebChromeClient(new WebChromeClient());
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.getSettings().setDatabaseEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setJavaScriptEnabled(true);
            // Set WebChromeClient to handle file upload
            webView.setWebChromeClient(new WebChromeClient() {
                // For Android versions 5.0 and above (API >= 21), use this method
                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null); // cancel a previous, unanswered request
                    }
                    uploadMessage = filePathCallback;
                    setUrl(_url);
                    return openFileChooser(fileChooserParams);
                }
            });
            webView.setPadding(0, 0, 0, 0);
            webView.getSettings().setDefaultTextEncodingName("utf-8");
            webView.addJavascriptInterface(new JavaScriptInterface(getContext(), webView), "Android");
            clearWebCacheAfterUpdate();
            loadCurrentPage();
            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                    if(url.startsWith("blob:")){
                        webView.evaluateJavascript(JavaScriptInterface.getBase64StringFromBlobUrl(url, mimeType), null);
                    } else {
                        downloadFile(url, userAgent, contentDisposition, mimeType);
                    }
                }
            });

        } else {
            getAlertWithOkForActivity(NO_INTERNET);
        }
    }

    /**
     * The server sends images (dashboard icons) without Cache-Control, so the WebView kept old
     * icons for weeks after they were replaced. Start each new app version with an empty cache.
     */
    private void clearWebCacheAfterUpdate() {
        android.content.SharedPreferences prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE);
        if (prefs.getInt("web_cache_version", 0) != BuildConfig.VERSION_CODE) {
            webView.clearCache(true);
            prefs.edit().putInt("web_cache_version", BuildConfig.VERSION_CODE).apply();
        }
    }

    /** Loads the page chosen in the session (Home when none), signed in with the user's token. */
    private void loadCurrentPage() {
        String token = sessionManager.getStudentToken() != null ?
                sessionManager.getStudentToken() : sessionManager.getUserToken();
        String page = getLabel(1);
        String webUrl = page + (page.contains("?") ? "&" : "?")
                + "college_code=" + Uri.encode(sessionManager.getCollegeCode())
                + "&session_id=" + Uri.encode(sessionManager.getSessionId())
                + "&token=" + Uri.encode(token);
        Log.i("WEB", "Loading " + page); // never log the token
        webView.loadUrl(webUrl);
    }

    /** True for the school website's own login page, which must never show inside the app. */
    private boolean isWebLoginPage(Uri uri) {
        if (uri == null || uri.getHost() == null || !uri.getHost().equalsIgnoreCase(ApiClient.serverHost())) {
            return false;
        }
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase();
        return WEB_LOGIN_PATHS.contains(path);
    }

    /** The website asked for a fresh login: sign out of the app and show the app's own login screen. */
    private void onSessionExpired() {
        if (sessionExpiredHandled || getActivity() == null) return;
        sessionExpiredHandled = true;
        Toast.makeText(context, R.string.session_expired, Toast.LENGTH_LONG).show();
        sessionManager.logout();
    }

    /** Back from a menu page returns to Home; Home's history starts fresh so Back then asks to exit. */
    private void showHome() {
        sessionManager.setMenuUrl(null);
        clearHistoryOnLoad = true;
        loadCurrentPage();
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getLabel(0));
        }
    }

    /** tel:, mailto:, WhatsApp, UPI and intent:// links open in their own apps. */
    private void openExternally(Uri uri) {
        try {
            Intent intent;
            if ("intent".equals(uri.getScheme())) {
                intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setSelector(null);
            } else {
                intent = new Intent(Intent.ACTION_VIEW, uri);
            }
            startActivity(intent);
        } catch (Exception e) {
            Log.w("WEB", "No app for " + uri.getScheme(), e);
            Toast.makeText(context, "No app found to open this link", Toast.LENGTH_SHORT).show();
        }
    }

    // Launches the system file picker; honours the page's accept="..." and multiple attributes.
    private boolean openFileChooser(WebChromeClient.FileChooserParams params) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] accept = params != null ? params.getAcceptTypes() : null;
        if (accept != null && accept.length > 0 && accept[0] != null && !accept[0].isEmpty()) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, accept);
        }
        if (params != null && params.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        try {
            fileChooserLauncher.launch(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No file picker app found", Toast.LENGTH_SHORT).show();
            uploadMessage = null;
            return false;
        }
    }

    public void getAlertWithOkForActivity(String msg) {
        if (!isAdded()) return;
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
    public void onDestroyView() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setDownloadListener(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(getLabel(0));
        }
    }

    protected void getErrorImage(WebView view)  {
        String html = "<body style=\"margin:0;padding:0;border:0;\"><img width=\"100%\" src=\"file:///android_asset/page_not_found.jpg\" /></body>";
        view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", "about:blank");
    }

    class MywebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (isWebLoginPage(Uri.parse(url))) {
                view.stopLoading();
                onSessionExpired();
                return;
            }
            super.onPageStarted(view, url, favicon);
            if (webView == null) return;
            webView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (clearHistoryOnLoad) {
                clearHistoryOnLoad = false;
                view.clearHistory();
            }
            isLoaded = false;
            if (webView == null) return;
            webView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            // Override window.print to call AndroidPrint.print()
//            String jsOverridePrint = "javascript:(function() {" +
//                    "window.print = function() { Android.print(); };" +
//                    "})()";
//            webView.evaluateJavascript(jsOverridePrint, null);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Log.e("WebViewContent", "Error: " + error.getDescription());
            if (request.isForMainFrame() && error.getErrorCode() != -1) {
                getErrorImage(view);
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            List<Integer> errorCodes = List.of(400, 403, 404, 500, 503);
            if (request.isForMainFrame() && errorCodes.contains(errorResponse.getStatusCode())) {
                getErrorImage(view);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if (request.isForMainFrame() && isWebLoginPage(uri)) {
                onSessionExpired();
                return true;
            }
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "";
            if (!scheme.equals("http") && !scheme.equals("https") && !scheme.equals("about")
                    && !scheme.equals("data") && !scheme.equals("blob") && !scheme.equals("javascript")) {
                openExternally(uri);
                return true;
            }
            String menuName = myDatabase.getNameByUrl(uri.toString());
            if(menuName != null){
                if (getActivity() != null) {
                    Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(menuName);
                }
            }
            return false;
        }
    }

    private void downloadFile(String url, String userAgent, String contentDisposition, String mimeType) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setMimeType(mimeType);
            String cookies = CookieManager.getInstance().getCookie(url);
            if (cookies != null) request.addRequestHeader("Cookie", cookies);
            if (userAgent != null) request.addRequestHeader("User-Agent", userAgent);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            boolean canWritePublic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    || ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (canWritePublic) {
                // Public Downloads folder, so the user can find the file afterwards.
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            } else {
                request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);
            }

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(context, "Downloading " + fileName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Download Manager not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DOWNLOAD", "Download failed", e);
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUrl(String url){
        _url = url;
    }

    private String getLabel(int i){
        String [] attr;
        String menu = sessionManager.getMenuUrl();
        if (menu != null && menu.contains("::") && !menu.endsWith("::null")) {
            attr = menu.split("::", 2);
        } else {
            attr = new String[]{"Home", sessionManager.getLandingUrl()};
        }
        return attr[i] != null ? attr[i] : "";
    }
}