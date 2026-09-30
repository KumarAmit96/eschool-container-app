package com.tech.eskool;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tech.eskool.database.MyDatabase;
import com.tech.eskool.service.SessionManager;
import com.tech.eskool.util.ApiHandler;

import java.util.Map;

public class StartActivity extends AppCompatActivity implements Runnable{

    private static final String TAG = StartActivity.class.getName();
    public static final String NO_INTERNET = "NO INTERNET! PLEASE TRY AGAIN LATER";
    public static final String SOMETHING_WENT_WRONG = "SOMETHING WENT WRONG!!!";
    public static final String ALL_PERMISSION_REQUIRED = "All permission is required to use this application.";
    public static final String DOWNLOAD_PERMISSION = "You haven't enabled download permission. Please enable it!";

    SessionManager sessionManager;
    Activity activity;
    String code;
    Intent intent;
    MyDatabase myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        activity = StartActivity.this;
        sessionManager = new SessionManager(this);
        myDatabase = new MyDatabase(this, null, null, 0);

        intent = getIntent();
        if(sessionManager.checkConnectivity(this)) {
            getData();
            askNotificationPermission();
            findViewById(R.id.image).postDelayed(this, 5000);
        } else {
            getAlertWithOkForActivity(NO_INTERNET);
        }
    }

    public void getAlertWithOkForActivity(String msg)
    {

        AlertDialog.Builder alert = new AlertDialog.Builder(this,R.style.myAlertDialog);
        alert.setTitle(R.string.app_name);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        if(!msg.equals(NO_INTERNET)) {
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
        }
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void getData(){
        if(sessionManager.isLoggedIn()) {
            code = sessionManager.getCollegeCode();
            String token = sessionManager.getStudentToken() != null ?
                    sessionManager.getStudentToken() : sessionManager.getUserToken();
            String session = sessionManager.getSessionId() != null ?
                    sessionManager.getSessionId() : "";
            ApiHandler apiHandler = new ApiHandler(sessionManager, myDatabase);
            if(sessionManager.getStudentToken() != null ) {
                apiHandler.setSibling(token, session, code);
                apiHandler.setStudentMenuItem(code, token);
            }else{
                apiHandler.setUserMenuItem(code, token);
            }
            apiHandler.setSessionData(code);
        }
    }

    @Override
    public void run() {
        if(sessionManager.isLoggedIn())
        {
            Map<String,String> map = sessionManager.getUserDetails();
            String empType = map.get(SessionManager.EMPTYPE);
            code = sessionManager.getCollegeCode();
            try {
                if (empType != null) {
                    Intent empIntent = new Intent(this, TeacherDashBoardActivity.class);
                    if(intent != null){
                        empIntent.putExtra("TITLE", intent.getStringExtra("title"));
                        empIntent.putExtra("MESSAGE", intent.getStringExtra("message"));
                    }
                    startActivity(empIntent);
                    finish();
                } else {
                    Intent studentIntent = new Intent(this, StudentsDashboardActivity.class);
                    if(intent != null) {
                        studentIntent.putExtra("TITLE", intent.getStringExtra("title"));
                        studentIntent.putExtra("MESSAGE", intent.getStringExtra("message"));
                    }
                    startActivity(studentIntent);
                    finish();
                }
            }catch (Exception e) {
                Log.i(TAG, "ERROR => "+e.getLocalizedMessage());
            }
        }
        else {
            startActivity(new Intent(this, LoginTypeActivity.class));
            finish();
        }
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.i(TAG, "Access Granted");
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                    Toast.makeText(activity, "You can't get the notification. Enable it to receive it.", Toast.LENGTH_SHORT).show();
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
                Log.i(TAG, "Access Granted");
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                Toast.makeText(activity, "You can't get the notification. Enable it to receive it.", Toast.LENGTH_SHORT).show();
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}