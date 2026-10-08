package com.tech.eskool;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.tech.eskool.database.MyDatabase;
import com.tech.eskool.dto.SchoolLoginClass;
import com.tech.eskool.dto.StudentLoginClass;
import com.tech.eskool.rest_service.ApiClient;
import com.tech.eskool.rest_service.Api_Interface;
import com.tech.eskool.service.SessionManager;
import com.tech.eskool.util.ApiHandler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getName();

    /** True once we've already sent the user through VerifyActivity for this Submit tap. */
    private boolean verifyAttempted = false;

    /** Opens the server's browser check; on success, repeats the login automatically. */
    private final ActivityResultLauncher<android.content.Intent> verifyLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> onVerifyResult(result.getResultCode()));
    SessionManager sessionManager;
    EditText username,password, code_college;
    String UserName,PassWord, Code;
    ProgressDialog progressDialog;
    String type;
    LinearLayout forgot_layout;
    TextView loginText;
    MyDatabase myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDatabase = new MyDatabase(this,null,null,0);
        myDatabase.deleteMenuItem();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Android 16 (API 36) no longer calls onBackPressed(); handle back via the dispatcher.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        sessionManager = new SessionManager(this);
        if(sessionManager.checkConnectivity(this)) {
            username = findViewById(R.id.username_box);
            password = findViewById(R.id.password_box);
            code_college = findViewById(R.id.code_college);
            forgot_layout = findViewById(R.id.forgot_layout);
            loginText = findViewById(R.id.loginText);

            Intent intent = getIntent();
            if (intent != null) {
                type = intent.getStringExtra("TYPE");
                if (type == null) type = "PARENT";
                loginText.setText(type.concat(" ").concat("LOGIN"));
                if(!type.equals("PARENT"))
                    forgot_layout.setVisibility(View.GONE);
            }

            progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Processing...");
            progressDialog.setCancelable(false);
        } else {
            getAlertWithOkForActivity(StartActivity.NO_INTERNET);
        }
    }

    public void getAlertWithOkForActivity(String msg)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.myAlertDialog);
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
        AlertDialog dialog = alert.create();
        dialog.show();

    }

    public void callLogin(View v)
    {
        verifyAttempted = false;
        Log.setUser(code_college.getText().toString().trim() + "/" + username.getText().toString().trim());
        doLogin();
    }

    private void doLogin()
    {
        UserName = username.getText().toString().trim();
        PassWord = password.getText().toString().trim();
        Code = code_college.getText().toString().trim();
        if(UserName == null || UserName.isEmpty()) {
            username.setError("Please fill the username ");
        } else if(PassWord == null || PassWord.isEmpty())
        {
            password.setError("Please fill the password ");
        } else if(Code.isEmpty()) {
            code_college.setError("Please fill the eskool code");
        } else {
            String deviceType = "ANDROID";
            if(type.equals("PARENT"))
                getStudentLoginData(UserName,PassWord, Code,  deviceType);
            else {
                getUserFromServer(UserName, PassWord, Code,  deviceType);
                Log.i("LOGIN", "EMP : "+type);
            }
        }
    }

    private void getUserFromServer(String userName, String passWord, String codes, String type)
    {
        progressDialog.show();

        Api_Interface api_interface = ApiClient.getClient().newBuilder()
                .client(HttpClientProvider.get(this)).build().create(Api_Interface.class);

        Call<SchoolLoginClass> call= api_interface.getSchoolLogin(userName, passWord, codes, null, type);

        call.enqueue(new Callback<SchoolLoginClass>() {
            @Override
            public void onResponse(Call<SchoolLoginClass> call, Response<SchoolLoginClass> response) {

                try {
                    if(response.body()!=null){
                        if(response.body().getStatus())
                        {
                            getFCMToken(response.body().getResponse().getAppToken());
                            sessionManager.setLoginCredential(response.body().getResponse().getUserId(), response.body().getResponse().getUserName(),
                                    response.body().getResponse().getEmpId(), response.body().getResponse().getEmpType(), response.body().getResponse().getClassId(),
                                    response.body().getResponse().getSectionId(), response.body().getResponse().getToken(), response.body().getResponse().getAppToken(),
                                    codes, response.body().getResponse().getPhoto(), response.body().getResponse().getCollege_name(),
                                    response.body().getResponse().getLandingPage(), response.body().getResponse().getChangePasswordPage(),
                                    response.body().getResponse().getNotificationPage());
                            // Open the dashboard once the menu is stored, so the drawer isn't empty.
                            new ApiHandler(sessionManager, myDatabase).setUserMenuItem(codes, response.body().getResponse().getToken(),
                                    () -> openDashboard(TeacherDashBoardActivity.class));
                        } else {
                            getDialog();
                            sessionManager.getAlertWithOk(response.body().getMessage());
                        }
                    } else {
                        getDialog();
                        Log.e(TAG, "Employee login: HTTP " + response.code() + " " + response.message());
                        sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
                    }
                } catch (Exception e) {
                    progressDialog.dismiss();
                    Log.e(TAG, "Employee login: error handling response", e);
                    sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
                }
            }

            @Override
            public void onFailure(Call<SchoolLoginClass> call, Throwable t) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Log.e(TAG, "Employee login: request failed", t);
                if (isNotJson(t) && !verifyAttempted)
                    openVerification(call.request());
                else if (isNotJson(t))
                    showRawServerReply(call.request());
                else if (t instanceof java.io.IOException)
                    sessionManager.getAlertWithOk(getString(R.string.login_network_error));
                else
                    sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
            }
        });

    }

    private void getStudentLoginData(String userName, String passWord, String codes, String type)
    {
        progressDialog.show();

        Api_Interface api_interface = ApiClient.getClient().newBuilder()
                .client(HttpClientProvider.get(this)).build().create(Api_Interface.class);

        Call<StudentLoginClass> call = api_interface.getStudentLogin(userName, passWord, codes, null, type);

        call.enqueue(new Callback<StudentLoginClass>() {
            @Override
            public void onResponse(@NonNull Call<StudentLoginClass> call, @NonNull Response<StudentLoginClass> response) {
                try {
                    if(response.body()!=null)
                    {
                        Log.i("LOGIN", response.toString());
                        if(response.body().getStatus())
                        {
                            getFCMToken(response.body().getResponse().getAppToken());
                            sessionManager.setStudentLoginCredential(response.body().getResponse().getStudentId(), response.body().getResponse().getStudentName(),
                                    response.body().getResponse().getClassId(),response.body().getResponse().getSectionId(),
                                    response.body().getResponse().getToken(),response.body().getResponse().getPhoto(),response.body().getResponse().getSessionId(),
                                    response.body().getResponse().getAppToken(), codes, response.body().getResponse().getCollege_name(), response.body().getResponse().getPayment_gateway(),
                                    response.body().getResponse().getEnrollment_no(), response.body().getResponse().getLandingPage(),
                                    response.body().getResponse().getChangePasswordPage(), response.body().getResponse().getNotificationPage());
                            new ApiHandler(sessionManager, myDatabase).setStudentMenuItem(codes, response.body().getResponse().getToken(),
                                    () -> openDashboard(StudentsDashboardActivity.class));


                        }
                        else
                        {
                            getDialog();
                            sessionManager.getAlertWithOk(response.body().getMessage());
                        }
                    }
                    else
                    {
                        getDialog();
                        Log.e(TAG, "Student login: HTTP " + response.code() + " " + response.message());
                        sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
                    }
                }
                catch (Exception e)
                {
                    progressDialog.dismiss();
                    Log.e(TAG, "Student login: error handling response", e);
                    sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
                }

            }

            @Override
            public void onFailure(Call<StudentLoginClass> call, Throwable t) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Log.e(TAG, "Student login: request failed", t);
                if (isNotJson(t) && !verifyAttempted)
                    openVerification(call.request());
                else if (isNotJson(t))
                    showRawServerReply(call.request());
                else if (t instanceof java.io.IOException)
                    sessionManager.getAlertWithOk(getString(R.string.login_network_error));
                else
                    sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);

            }
        });

    }


    private void openDashboard(Class<?> dashboard)
    {
        if (isFinishing() || isDestroyed()) return;
        if (progressDialog.isShowing()) progressDialog.dismiss();
        Intent intent = new Intent(LoginActivity.this, dashboard);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishLoginType();
        finish();
    }

    private void getDialog()
    {
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        else
            progressDialog.show();
    }

    public void  getForgetCall(View v)
    {
        String code = code_college.getText().toString().trim();
        if (code.isEmpty()) {
            code_college.setError("Please fill the eskool code");
            code_college.requestFocus();
            return;
        }
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        intent.putExtra(ForgetPasswordActivity.EXTRA_COLLEGE_CODE, code);
        startActivity(intent);
    }

    private void finishLoginType(){
        if (LoginTypeActivity.ACTIVITY != null)
            LoginTypeActivity.ACTIVITY.finish();
    }

    private void getFCMToken(String appToken) {
        if (appToken == null || appToken.isEmpty()) {
            Log.w(TAG, "No app token: notifications not subscribed");
            return;
        }
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(appToken)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Subscribed to notifications");
                        } else {
                            Log.w(TAG, "Notification subscription failed", task.getException());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Notification subscription failed", e);
        }
    }

    /** True when the server replied with something that is not JSON (e.g. an HTML page). */
    private boolean isNotJson(Throwable t) {
        return t instanceof com.google.gson.JsonParseException
                || t instanceof com.google.gson.stream.MalformedJsonException
                || t instanceof java.io.EOFException
                || (t.getCause() instanceof com.google.gson.stream.MalformedJsonException);
    }

    /**
     * Repeats the same request and writes what the server really sent back (status, final URL,
     * server header, start of the body) to the server log. The user only sees SOMETHING_WENT_WRONG.
     */
    private void showRawServerReply(okhttp3.Request request) {
        HttpClientProvider.get(this).newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call c, @NonNull java.io.IOException e) {
                Log.e(TAG, "Diagnostic request failed", e);
                runOnUiThread(() -> sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG));
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call c, @NonNull okhttp3.Response r) throws java.io.IOException {
                String body = r.body() != null ? r.body().string() : "";
                Log.e(TAG, "Diagnostic reply " + r.code() + " " + r.request().url() + "\n" + body);
                String text = body.replaceAll("(?s)<(script|style)[^>]*>.*?</\\1>", " ")
                        .replaceAll("<[^>]+>", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
                if (text.length() > 400) text = text.substring(0, 400) + "...";
                String server = r.header("Server");
                String msg = "Server did not send JSON.\n\n"
                        + "HTTP " + r.code() + "\n"
                        + "URL: " + r.request().url().host() + r.request().url().encodedPath() + "\n"
                        + "Server: " + (server == null ? "-" : server) + "\n\n"
                        + text;
                Log.e(TAG, msg); // details go to the server log only
                runOnUiThread(() -> sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG));
            }
        });
    }

    /** Shows the server's verification page in a WebView, then retries (see verifyLauncher). */
    private void openVerification(okhttp3.Request request) {
        verifyAttempted = true;
        okhttp3.HttpUrl u = request.url();
        String url = u.scheme() + "://" + u.host() + "/";
        android.content.Intent i = new android.content.Intent(this, VerifyActivity.class);
        i.putExtra(VerifyActivity.EXTRA_URL, url);
        verifyLauncher.launch(i);
    }

    private void onVerifyResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            doLogin();
        } else {
            Log.w(TAG, "Firewall verification was not completed (result " + resultCode + ")");
            sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
        }
    }
}
