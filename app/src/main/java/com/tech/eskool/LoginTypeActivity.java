package com.tech.eskool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



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