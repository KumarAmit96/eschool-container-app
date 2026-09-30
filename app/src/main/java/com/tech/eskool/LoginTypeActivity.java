package com.tech.eskool;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class LoginTypeActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout parent, teacher, admin;
    public static Activity ACTIVITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ACTIVITY = LoginTypeActivity.this;
        setContentView(R.layout.activity_login_type);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Dexter.withActivity(LoginTypeActivity.this)
                .withPermissions(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.areAllPermissionsGranted())
                            Log.i("PERMISSION", "GRANTED");
                        else if(report.isAnyPermissionPermanentlyDenied())
                            Log.i("PERMISSION","NOT ALL GRANTED");/* ... */}
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();/* ... */}
                }).check();

        parent = findViewById(R.id.parent);
        teacher = findViewById(R.id.teacher);
        admin = findViewById(R.id.admin);

        teacher.setOnClickListener(this);
        parent.setOnClickListener(this);
        admin.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        int id=v.getId();
        Intent intent=new Intent(this, LoginActivity.class);
        if(id==R.id.parent){
            intent.putExtra("TYPE","PARENT");
        }else if(id == R.id.teacher){
            intent.putExtra("TYPE","TEACHER");
        }
        else if(id == R.id.admin){
            intent.putExtra("TYPE","ADMIN");
        }
        startActivity(intent);

    }

}