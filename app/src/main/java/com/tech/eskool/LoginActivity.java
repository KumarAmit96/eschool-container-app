package com.tech.eskool;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
        UserName = username.getText().toString();
        PassWord = password.getText().toString();
        Code = code_college.getText().toString();
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

        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);

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
                            new ApiHandler(sessionManager, myDatabase).setUserMenuItem(codes, response.body().getResponse().getToken());
                            Thread.sleep(3000);
                            getDialog();
                            Intent intent = new Intent(LoginActivity.this, TeacherDashBoardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finishLoginType();
                            finish();
                        } else {
                            getDialog();
                            sessionManager.getAlertWithOk(response.body().getMessage());
                        }
                    } else {
                        getDialog();
                        sessionManager.getAlertWithOk(response.message());
                    }
                } catch (Exception e) {
                    progressDialog.dismiss();
                    assert response.body() != null;
                    sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
                }
            }

            @Override
            public void onFailure(Call<SchoolLoginClass> call, Throwable t) {
                getDialog();
                sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
            }
        });

    }

    private void getStudentLoginData(String userName, String passWord, String codes, String type)
    {
        progressDialog.show();

        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);

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
                            new ApiHandler(sessionManager, myDatabase).setStudentMenuItem(codes, response.body().getResponse().getToken());
                            Thread.sleep(3000);
                            getDialog();
                            Intent intent = new Intent(LoginActivity.this, StudentsDashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finishLoginType();
                            finish();


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
                        sessionManager.getAlertWithOk(response.message());
                    }
                }
                catch (Exception e)
                {
                    progressDialog.dismiss();
                    sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);
                }

            }

            @Override
            public void onFailure(Call<StudentLoginClass> call, Throwable t) {
                getDialog();
                sessionManager.getAlertWithOk(StartActivity.SOMETHING_WENT_WRONG);

            }
        });

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
        startActivity(new Intent(this, ForgetPasswordActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void finishLoginType(){
        LoginTypeActivity.ACTIVITY.finish();
    }

    private void getFCMToken(String appToken) {

        FirebaseMessaging.getInstance().subscribeToTopic(appToken)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Successfully subscribed...";
                        if (!task.isSuccessful()) {
                            msg = "Not subscribed successfully...";
                            sessionManager.logoutUser();
                            return;
                        }
                        Log.d(TAG, msg);
                    }
                });

    }
}