package com.tech.eskool;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.tech.eskool.adapter.CustomNavigationViewAdapter;
import com.tech.eskool.adapter.SiblingAdapter;
import com.tech.eskool.database.MyDatabase;
import com.tech.eskool.databinding.ActivityStudentsDashboardBinding;
import com.tech.eskool.dto.MenuResponse;
import com.tech.eskool.dto.StudentLoginResponse;
import com.tech.eskool.dto.StudentSiblingRecord;
import com.tech.eskool.rest_service.ApiClient;
import com.tech.eskool.rest_service.Api_Interface;
import com.tech.eskool.service.SessionManager;
import com.tech.eskool.util.ApiHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentsDashboardActivity extends AppCompatActivity {

    AppBarConfiguration mAppBarConfiguration;
    SessionManager sessionManager;
    NavigationView navigationView;
    NavController navController;
    MyDatabase myDatabase;
    DrawerLayout drawer;
    CircularImageView imageView;
    Map<String, String> map;
    Spinner spinner_session;
    ArrayList<StudentLoginResponse> sessionResponseArrayList;
    SiblingAdapter sessionCustomAdapter;
    TextView name, designation;
    LinearLayout sibling_layout;
    ExpandableListView multiNav;
    CustomNavigationViewAdapter customNavigationViewAdapter;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        myDatabase = new MyDatabase(this, null, null, 0);
        sessionResponseArrayList = new ArrayList<>();
        try {
            Intent intent = getIntent();
            if (!Objects.requireNonNull(intent.getStringExtra("MESSAGE")).isEmpty()) {
                sessionManager.getNotificationAlertWithOk(intent.getStringExtra("TITLE"), intent.getStringExtra("MESSAGE"));
            }
        }catch (Exception e)
        {
            Log.i("NOTIFICATION_TEA", "ERROR => "+e.getLocalizedMessage());
        }

        ActivityStudentsDashboardBinding binding = ActivityStudentsDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarStudentsDashboard.toolbar);

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        multiNav = binding.multiNav;
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_menu)
                .setOpenableLayout(drawer)
                .build();

        name = (TextView) navigationView.findViewById(R.id.name);
        designation = (TextView) navigationView.findViewById(R.id.designation);
        designation.setVisibility(View.GONE);
        imageView = navigationView.findViewById(R.id.imageView);
        map = sessionManager.getStudentDetails();

        Picasso.get().load(map.get(SessionManager.STUDENT_PHOTO)).into(imageView);
        name.setText(map.get(SessionManager.STUDENT_NAME));
        spinner_session = (Spinner) navigationView.findViewById(R.id.session);
        sibling_layout = navigationView.findViewById(R.id.sibling_layout);
        StudentLoginResponse studentLoginResponse = new StudentLoginResponse();
        studentLoginResponse.setStudentId("-1");
        studentLoginResponse.setSectionId("-1");
        studentLoginResponse.setClassId("-1");
        studentLoginResponse.setSessionId("-1");
        studentLoginResponse.setStudentName(" SIBLING ");
        studentLoginResponse.setAppToken(null);
        studentLoginResponse.setToken(null);
        studentLoginResponse.setPhoto(null);
        sessionResponseArrayList.add(count, studentLoginResponse);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_students_dashboard);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        getMenuItem();
        setInVisible();
        getSibling();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.students_dashboard, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_students_dashboard);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void getMenuItem()
    {
        Map<Integer, List<MenuResponse>> menuMap = myDatabase.getMenuItem()
                .stream().collect(Collectors.groupingBy(MenuResponse::getParentId));
        if(!menuMap.isEmpty()) {
            multiNav.setGroupIndicator(null);
            List<MenuResponse> parentMenu = menuMap.get(0);
            customNavigationViewAdapter = new CustomNavigationViewAdapter(this, parentMenu, menuMap);
            multiNav.setAdapter(customNavigationViewAdapter);

            multiNav.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
                String link = myDatabase.getMenuUrlByName(menuMap.get(parentMenu.get(groupPosition).getId()).get(childPosition).getName());
                sessionManager.setMenuUrl(link);
                navController.navigate(R.id.nav_menu);
                drawer.closeDrawer(GravityCompat.START);
                return false;
            });

            multiNav.setOnGroupClickListener((parent, v, groupPosition, id) -> {
                MenuResponse groupItem = (MenuResponse) customNavigationViewAdapter.getGroup(groupPosition);
                int childrenCount = customNavigationViewAdapter.getChildrenCount(groupPosition);
                if (childrenCount == 0) {
                    String link = groupItem.getName().concat("::").concat(groupItem.getUrl());
                    sessionManager.setMenuUrl(link);
                    navController.navigate(R.id.nav_menu);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            });
        }
    }
    private void setInVisible(){
        sessionCustomAdapter = new SiblingAdapter( sessionResponseArrayList, this);
        spinner_session.setAdapter(sessionCustomAdapter);
        spinner_session.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if(position != 0){
                        getSibling(sessionResponseArrayList.get(position).getStudentId());
                    }else {
                        Log.i("SIBLING", "NOT SELECTED");
                    }

                } catch (Exception e) {
                    Log.i("SIBLING", "ERROR => "+e.getLocalizedMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i("SIBLING", "NOT SELECTED");
            }
        });
    }
    private void getSibling()
    {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        Call<StudentSiblingRecord> call = api_interface.getSiblingData(sessionManager.getStudentDetails().get(SessionManager.STUDENT_TOKEN), sessionManager.getStudentDetails().get(SessionManager.SESSIONID), sessionManager.getCollegeCode());
        call.enqueue(new Callback<StudentSiblingRecord>() {
            @Override
            public void onResponse(@NonNull Call<StudentSiblingRecord> call, @NonNull Response<StudentSiblingRecord> response) {
                try {
                    if(response.body()!=null)
                    {
                        if(response.body().getStatus())
                        {
                            if(!response.body().getResponse().isEmpty())
                            {
                                for(StudentLoginResponse student : response.body().getResponse()) {
                                    count++;
                                    sessionResponseArrayList.add(count, student);
                                    myDatabase.setDataIntoSiblingTable(student);
                                }
                                sessionCustomAdapter.notifyDataSetChanged();
                            }else
                            {
                                Log.i("SIBILING", "NO RECORD FOUND!");
                            }
                        }
                        else
                        {
                            sessionManager.getSessionLogout();
                            Log.i("TIME TABLE", response.body().getMessage());
                        }
                    }
                    else
                    {
                        assert response.body() != null;
                        Log.i("TIME TABLE", response.body().getMessage());
                    }
                }
                catch (Exception e)
                {
                    assert response.body() != null;
                    Log.i("TIME TABLE", response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<StudentSiblingRecord> call, Throwable t) {
                Log.i("TIME TABLE", Objects.requireNonNull(t.getLocalizedMessage()));

            }
        });
    }

    private void getSibling(String getId)
    {
        StudentLoginResponse studentLoginResponse = myDatabase.getSiblingData(getId);
        String collegeCode = sessionManager.getCollegeCode();
        String collegeName = sessionManager.getCollegeName();

        sessionManager.cleanSessionData();
        sessionManager.setStudentLoginCredential(studentLoginResponse.getStudentId(), studentLoginResponse.getStudentName(),
                studentLoginResponse.getClassId(),studentLoginResponse.getSectionId(),studentLoginResponse.getToken(),
                studentLoginResponse.getPhoto(),studentLoginResponse.getSessionId(), studentLoginResponse.getAppToken(),
                collegeCode, collegeName, studentLoginResponse.getPayment_gateway(), studentLoginResponse.getEnrollment_no(),
                studentLoginResponse.getLandingPage(), studentLoginResponse.getChangePasswordPage(),
                studentLoginResponse.getNotificationPage());
        myDatabase.deleteSiblingData();
        myDatabase.deleteMenuItem();
        new ApiHandler(sessionManager, myDatabase).setUserMenuItem(collegeCode, studentLoginResponse.getToken());
        FirebaseMessaging.getInstance().subscribeToTopic(sessionManager.getStudentAppToken())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Successfully subscribed...";
                        if (!task.isSuccessful()) {
                            msg = "Not subscribed successfully...";
                            sessionManager.logoutUser();
                            return;
                        }
                        Log.d("SIBILING ENTRY", msg);

                    }
                });

        sessionManager.getSiblingEntry();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_settings)
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.myAlertDialog);
            alert.setTitle(R.string.app_name);
            alert.setIcon(R.mipmap.ic_launcher);
            alert.setMessage("Do you want to Logout?");
            alert.setCancelable(false);
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    sessionManager.getSessionLogout();
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();

        }else if(id == R.id.nav_home){
            sessionManager.setMenuUrl(null);
            navController.navigate(R.id.nav_menu);
        } else if (id == R.id.action_change_password) {
            sessionManager.setMenuUrl("Change Password::".concat(sessionManager.getChangePassword()));
            navController.navigate(R.id.nav_menu);
        }
        return super.onOptionsItemSelected(item);
    }

}