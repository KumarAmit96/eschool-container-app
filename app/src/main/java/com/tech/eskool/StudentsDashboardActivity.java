package com.tech.eskool;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
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
        sessionManager.prepareDashboardStart(getIntent(), savedInstanceState == null);

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

        String photo = map.get(SessionManager.STUDENT_PHOTO);
        if (photo != null && !photo.trim().isEmpty()) {
            Picasso.get().load(photo).placeholder(R.drawable.ic_teacher).error(R.drawable.ic_teacher).into(imageView);
        }
        name.setText(map.get(SessionManager.STUDENT_NAME));
        spinner_session = (Spinner) navigationView.findViewById(R.id.session);
        sibling_layout = navigationView.findViewById(R.id.sibling_layout);
        StudentLoginResponse studentLoginResponse = new StudentLoginResponse();
        studentLoginResponse.setStudentId("-1");
        studentLoginResponse.setSectionId("-1");
        studentLoginResponse.setClassId("-1");
        studentLoginResponse.setSessionId("-1");
        studentLoginResponse.setStudentName(getString(R.string.switch_sibling));
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
        sessionCustomAdapter = new SiblingAdapter(sessionResponseArrayList, this, map.get(SessionManager.STUDENT_ID));
        spinner_session.setAdapter(sessionCustomAdapter);
        // Let the whole pill (icon + label + arrow) open the dropdown, not just the label.
        sibling_layout.setOnClickListener(v -> spinner_session.performClick());
        spinner_session.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position != 0 && sessionCustomAdapter.isCurrentStudent(position)) {
                        // Already viewing this student; reset to the placeholder.
                        spinner_session.setSelection(0);
                    } else if(position != 0){
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
                    StudentSiblingRecord body = response.body();
                    if (body == null) {
                        Log.w("SIBLING", "HTTP " + response.code() + " " + response.message());
                    } else if (!Boolean.TRUE.equals(body.getStatus())) {
                        Log.w("SIBLING", "status false: " + body.getMessage());
                        sessionManager.getSessionLogout();
                    } else if (body.getResponse() == null || body.getResponse().isEmpty()) {
                        Log.i("SIBLING", "NO RECORD FOUND!");
                    } else {
                        myDatabase.deleteSiblingData();
                        for (StudentLoginResponse student : body.getResponse()) {
                            count++;
                            sessionResponseArrayList.add(count, student);
                            myDatabase.setDataIntoSiblingTable(student);
                        }
                        sessionCustomAdapter.notifyDataSetChanged();
                        sibling_layout.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Log.e("SIBLING", "Error handling response", e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StudentSiblingRecord> call, @NonNull Throwable t) {
                Log.e("SIBLING", "Request failed", t);
            }
        });
    }

    private void getSibling(String getId)
    {
        StudentLoginResponse studentLoginResponse = myDatabase.getSiblingData(getId);
        if (studentLoginResponse == null) {
            Toast.makeText(this, "Could not switch student. Please try again.", Toast.LENGTH_SHORT).show();
            spinner_session.setSelection(0);
            return;
        }
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
        String topic = sessionManager.getStudentAppToken();
        if (topic != null && !topic.isEmpty()) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(task -> Log.d("SIBLING ENTRY",
                            task.isSuccessful() ? "Subscribed to notifications" : "Notification subscription failed"));
        }

        sessionManager.getSiblingEntry();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_settings)
        {
            sessionManager.getSessionLogout();
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