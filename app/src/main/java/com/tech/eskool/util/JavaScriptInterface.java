package com.tech.eskool.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.tech.eskool.Log;
import com.tech.eskool.service.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class JavaScriptInterface {

    private static final String TAG = "JSInterface";
    private static final String DEFAULT_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final Context context;
    private final SessionManager sessionManager;
    private final WebView webView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public JavaScriptInterface(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
        sessionManager = new SessionManager(context);
    }

    /** Called from the page (JavaBridge thread) with a "data:<mime>;base64,<data>" string. */
    @JavascriptInterface
    public void getBase64FromBlobData(String dataUrl) {
        try {
            Uri uri = saveDataUrl(dataUrl);
            mainHandler.post(() -> {
                if (uri != null) {
                    sessionManager.setBlobFile(String.valueOf(uri));
                    Toast.makeText(context, "Downloaded successfully", Toast.LENGTH_SHORT).show();
                    sessionManager.showDownloadCompleteDialog(uri);
                } else {
                    sessionManager.setBlobFile(null);
                    Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Saving blob download failed", e);
            mainHandler.post(() -> Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show());
        }
    }

    @JavascriptInterface
    public void print() {
        // WebView may only be touched on the UI thread.
        mainHandler.post(() -> {
            PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
            if (printManager != null) {
                PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("WebViewPrint");
                PrintAttributes.Builder builder = new PrintAttributes.Builder();
                builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
                builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
                printManager.print("Document", printAdapter, builder.build());
            }
        });
    }

    /** JavaScript that reads a blob: URL and hands its contents to {@link #getBase64FromBlobData}. */
    public static String getBase64StringFromBlobUrl(String blobUrl, String mimeType) {
        if (blobUrl == null || !blobUrl.startsWith("blob")) {
            return "console.log('It is not a Blob URL');";
        }
        String type = (mimeType == null || mimeType.isEmpty()) ? DEFAULT_MIME : mimeType;
        return "(function(){var xhr=new XMLHttpRequest();" +
                "xhr.open('GET', '" + blobUrl.replace("'", "\\'") + "', true);" +
                "xhr.responseType = 'blob';" +
                "xhr.onload = function(e) {" +
                "    if (this.status == 200) {" +
                "        var blob = this.response;" +
                "        if (!blob.type) { blob = new Blob([blob], {type: '" + type + "'}); }" +
                "        var reader = new FileReader();" +
                "        reader.onloadend = function() { Android.getBase64FromBlobData(reader.result); };" +
                "        reader.readAsDataURL(blob);" +
                "    }" +
                "};" +
                "xhr.send();})();";
    }

    /** Writes the data URL to Downloads; returns a content:// Uri other apps can open, or null. */
    private Uri saveDataUrl(String dataUrl) throws Exception {
        if (dataUrl == null) return null;
        int comma = dataUrl.indexOf(',');
        String header = comma > 0 ? dataUrl.substring(0, comma) : "";
        String mime = DEFAULT_MIME;
        if (header.startsWith("data:")) {
            int semi = header.indexOf(';');
            String parsed = header.substring(5, semi > 5 ? semi : header.length());
            if (!parsed.isEmpty()) mime = parsed;
        }
        byte[] bytes = Base64.decode(comma > 0 ? dataUrl.substring(comma + 1) : dataUrl, Base64.DEFAULT);

        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        if (ext == null) ext = "xlsx";
        String fileName = "eskool_" + System.currentTimeMillis() + "." + ext;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, mime);
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return null;
            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                if (out == null) return null;
                out.write(bytes);
            }
            return uri;
        }

        // Android 7-9: app-specific Downloads folder (no storage permission needed).
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) return null;
        File file = new File(dir, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        }
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }
}
