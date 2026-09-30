package com.tech.eskool.ui.item;

import static android.app.Activity.RESULT_OK;
import static com.tech.eskool.StartActivity.NO_INTERNET;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.tech.eskool.R;
import com.tech.eskool.database.MyDatabase;
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        sessionManager = new SessionManager(context);
        myDatabase = new MyDatabase(context, null, null, 0);
        // Request permissions if needed
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 1);
        }

        fileChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Uri resultUri = data == null ? null : data.getData();
                        if (uploadMessage != null) {
                            if (resultUri != null) {
                                uploadMessage.onReceiveValue(new Uri[]{resultUri});
                            } else {
                                uploadMessage.onReceiveValue(null);
                            }
                        }
                        uploadMessage = null;
                    }
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
                if (webView.canGoBack()) {
                    webView.goBack();
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
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
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
                    uploadMessage = filePathCallback;
                    setUrl(_url);
                    openFileChooser();
                    return true;
                }
            });
            webView.setPadding(0, 0, 0, 0);
            webView.getSettings().setDefaultTextEncodingName("utf-8");
            webView.addJavascriptInterface(new JavaScriptInterface(getContext(), webView), "Android");
            String token = sessionManager.getStudentToken() != null ?
                    sessionManager.getStudentToken() : sessionManager.getUserToken();
            String webUrl = getLabel(1) + "?college_code=" + sessionManager.getCollegeCode() + "&session_id=" + sessionManager.getSessionId() + "&token=" + token;
            Log.i("WEB", webUrl);
            webView.loadUrl(webUrl);
            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                    if(url.startsWith("blob:")){
                        String js = JavaScriptInterface.getBase64StringFromBlobUrl(url);
                        webView.loadUrl(js);
                        if(sessionManager.getBlobFile() != null){
                            sessionManager.showDownloadCompleteDialog(Uri.parse(sessionManager.getBlobFile()));
                        }
                    } else {
                        downloadFile(url);
                    }
                }
            });

        } else {
            getAlertWithOkForActivity(NO_INTERNET);
        }
    }

    // Call this method to launch the file chooser
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // Set the MIME type as needed
        fileChooserLauncher.launch(intent);
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
    public void onDestroyView() {
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
            super.onPageStarted(view, url, favicon);
            webView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            isLoaded = false;
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
            String menuName = myDatabase.getNameByUrl(request.getUrl().toString());
            if(menuName != null){
                if (getActivity() != null) {
                    Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(menuName);
                }
            }
            return false;
        }
    }

    private void downloadFile(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Downloading File");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Ensure the directory is valid for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, you can't write directly to external storage like before.
            // You'll need to use MediaStore or app-specific directories.
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "downloaded_file." + getFileExtension(url));
        } else {
            // For older versions, this works fine.
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_file." + getFileExtension(url));
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Download Manager not available", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(String url) {
        if (url.lastIndexOf(".") != -1 && url.lastIndexOf(".") != url.length() - 1) {
            return url.substring(url.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    private void setUrl(String url){
        _url = url;
    }

    private String getLabel(int i){
        String [] attr;
        if(sessionManager.getMenuUrl() != null) {
            attr = sessionManager.getMenuUrl().split("::");
        } else {
            attr = new String[]{"Home", sessionManager.getLandingUrl()};
        }
        return attr[i];
    }
}