package com.tech.eskool.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;


import com.tech.eskool.R;
import com.tech.eskool.service.SessionManager;

import java.io.IOException;
import java.io.OutputStream;

public class JavaScriptInterface {

    Context context;
    SessionManager sessionManager;
    WebView webView;

    public JavaScriptInterface(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
        sessionManager = new SessionManager(context);
    }

    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data) throws IOException {
        convertBase64StringToPdfAndStoreIt(base64Data);
    }

    @JavascriptInterface
    public void print() {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        if (printManager != null) {
            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("WebViewPrint");
            PrintAttributes.Builder builder = new PrintAttributes.Builder();
            builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
            builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            printManager.print("Document", printAdapter, builder.build());
        }
    }

    public static String getBase64StringFromBlobUrl(String blobUrl) {
        if (blobUrl.startsWith("blob")) {
            return "javascript: var xhr=new XMLHttpRequest();" +
                    "xhr.open('GET', '" + blobUrl + "', true);" +
                    "xhr.setRequestHeader('Content-type','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8');" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    if (this.status == 200) {" +
                    "        var blobPdf = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobPdf);" +
                    "        reader.onloadend = function() {" +
                    "            base64data = reader.result;" +
                    "            Android.getBase64FromBlobData(base64data);" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();";


        }
        return "javascript: console.log('It is not a Blob URL');";
    }

    private void convertBase64StringToPdfAndStoreIt(String base64Pdf) throws IOException {
        String base64Data = base64Pdf.replaceFirst("^data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,", "");
        byte[] pdfAsBytes = Base64.decode(base64Data, Base64.DEFAULT);
        String fileName = "my_xml_" + System.currentTimeMillis() + ".xlsx";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        }

        if (uri != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(pdfAsBytes);
                    outputStream.flush();
                    sessionManager.setBlobFile(String.valueOf(uri));
                    Toast.makeText(context, "Downloaded Successfully...", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            sessionManager.setBlobFile(null);
        }
    }



}
