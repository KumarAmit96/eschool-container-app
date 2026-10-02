package com.tech.eskool;

import static com.tech.eskool.StartActivity.NO_INTERNET;
import static com.tech.eskool.StartActivity.SOMETHING_WENT_WRONG;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tech.eskool.dto.ForgetResponse;
import com.tech.eskool.dto.OtpResponse;
import com.tech.eskool.rest_service.ApiClient;
import com.tech.eskool.rest_service.Api_Interface;
import com.tech.eskool.service.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgetPasswordActivity";
    RelativeLayout password_layout,enrollment;
    EditText enroll_detail,pass_new,otp;
    Button submit, submit_otp;
    ProgressDialog progressDialog;
    Context context;
    SessionManager sessionManager;
    TextView otp_pass,message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        context = ForgetPasswordActivity.this;
        sessionManager = new SessionManager(this);

        if(sessionManager.checkConnectivity(this)) {
            progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);

            password_layout = findViewById(R.id.password_layout);
            enrollment = findViewById(R.id.enrollment);

            message = findViewById(R.id.message);
            otp_pass = findViewById(R.id.otp_pass);

            enroll_detail = findViewById(R.id.enroll_detail);
            pass_new = findViewById(R.id.pass_new);
            otp = findViewById(R.id.otp);

            submit = findViewById(R.id.submit);
            submit_otp = findViewById(R.id.submit_otp);

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (otp.getText().toString().isEmpty()) {
                        otp.setError("Please fill the box");
                    } else {
                        if (pass_new.getText().toString().isEmpty()) {
                            pass_new.setError("Please fill the box");
                        } else {
                            if (sessionManager.isLoggedIn())
                                getChangePassword(otp.getText().toString(), pass_new.getText().toString());
                            else
                                getNewPassword(otp.getText().toString(), pass_new.getText().toString());
                        }
                    }

                }
            });

            submit_otp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    enroll_detail.getText().toString();
                    if (!enroll_detail.getText().toString().isEmpty()) {
                        getOTP(enroll_detail.getText().toString());
                    } else {
                        enroll_detail.setError("Please fill the box");
                    }
                }
            });

            if (sessionManager.isLoggedIn()) {
                getPasswordlayout();
            } else {
                message.setText("Enter your OTP and new password below");
                otp_pass.setText("OTP");
                getEnrollment();
            }
        }else {
            getAlertWithOkForActivity(NO_INTERNET);
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

    private void getEnrollment()
    {
        password_layout.setVisibility(View.GONE);
        enrollment.setVisibility(View.VISIBLE);
    }

    private void getPasswordlayout()
    {
        password_layout.setVisibility(View.VISIBLE);
        enrollment.setVisibility(View.GONE);
    }

    private void getProgress()
    {
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        else
            progressDialog.show();
    }

    private void getOTP(String enrollment)
    {
        getProgress();
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        String code = sessionManager.getCollegeCode();

        Call<OtpResponse> call = api_interface.getForgetPasswordDetail(enrollment,code);

        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                try {
                    if(response.body()!=null) {
                        if(response.body().getStatus()){
                            getProgress();
                            getPasswordlayout();
                        } else {
                            getProgress();
                            sessionManager.getSessionLogout();
                        }
                    } else{
                        getProgress();
                        Log.e(TAG, "Get OTP: HTTP " + response.code() + " " + response.message());
                        sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);
                    }
                }
                catch (Exception e){
                    progressDialog.dismiss();
                    Log.e(TAG, "Get OTP: error handling response", e);
                    sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {

                getProgress();
                Log.e(TAG, "Get OTP: request failed", t);
                sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);

            }
        });

    }

    private void getNewPassword(String otp, String newPassword)
    {
        getProgress();
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        String code = sessionManager.getCollegeCode();

        Call<ForgetResponse> call = api_interface.getOTPPasswordDetail(otp, newPassword,code);

        call.enqueue(new Callback<ForgetResponse>() {
            @Override
            public void onResponse(Call<ForgetResponse> call, Response<ForgetResponse> response) {
                try {
                    if(response.body()!=null)
                    {
                        if(response.body().getStatus())
                        {
                            getProgress();

                            new AlertDialog.Builder(ForgetPasswordActivity.this)
                                    .setTitle(R.string.app_name)
                                    .setMessage(response.body().getResponse())
                                    .setCancelable(false)
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Continue with delete operation
                                            startActivity(new Intent(ForgetPasswordActivity.this, StartActivity.class));
                                            finish();

                                        }
                                    })
                                    .setIcon(R.mipmap.ic_launcher)
                                    .show();

                        }
                        else
                        {
                            getProgress();
                            Log.w(TAG, "New password: server returned status false: " + response.body().getMessage());
                            sessionManager.getAlertWithOk(response.body().getMessage() != null
                                    ? response.body().getMessage() : SOMETHING_WENT_WRONG);
                        }
                    }
                    else
                    {
                        getProgress();
                        Log.e(TAG, "New password: HTTP " + response.code() + " " + response.message());
                        sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);
                    }
                }
                catch (Exception e)
                {
                    progressDialog.dismiss();
                    Log.e(TAG, "New password: error handling response", e);
                    sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);
                }
            }

            @Override
            public void onFailure(Call<ForgetResponse> call, Throwable t) {

                getProgress();
                Log.e(TAG, "New password: request failed", t);
                sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);

            }
        });

    }

    private void getChangePassword(String old_pass, String new_pass) {
        getProgress();
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);

        Call<ForgetResponse> call = api_interface.getResetPasswordDetail(sessionManager.getStudentDetails().get(SessionManager.STUDENT_TOKEN),sessionManager.getSessionId(),
                old_pass,new_pass, sessionManager.getCollegeCode());

        call.enqueue(new Callback<ForgetResponse>() {
            @Override
            public void onResponse(Call<ForgetResponse> call, Response<ForgetResponse> response) {
                try {
                    if(response.body()!=null)
                    {
                        if(response.body().getStatus())
                        {
                            getProgress();

                            new AlertDialog.Builder(ForgetPasswordActivity.this)
                                    .setTitle(R.string.app_name)
                                    .setMessage(response.body().getResponse())
                                    .setCancelable(false)
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Continue with delete operation
                                            startActivity(new Intent(ForgetPasswordActivity.this, StudentsDashboardActivity.class));
                                            finish();

                                        }
                                    })
                                    .setIcon(R.mipmap.ic_launcher)
                                    .show();

                        }
                        else
                        {
                            getProgress();
                            sessionManager.getAlertWithOk(response.body().getMessage());
                        }
                    }
                    else
                    {
                        getProgress();
                        Log.e(TAG, "Change password: HTTP " + response.code() + " " + response.message());
                        sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);
                    }
                }
                catch (Exception e)
                {
                    progressDialog.dismiss();
                    Log.e(TAG, "Change password: error handling response", e);
                    sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);
                }
            }

            @Override
            public void onFailure(Call<ForgetResponse> call, Throwable t) {
                getProgress();
                Log.e(TAG, "Change password: request failed", t);
                sessionManager.getAlertWithOk(SOMETHING_WENT_WRONG);
            }
        });
    }

}