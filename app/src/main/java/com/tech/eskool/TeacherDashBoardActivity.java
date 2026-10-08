package com.tech.eskool;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.tech.eskool.adapter.CustomNavigationViewAdapter;
import com.tech.eskool.adapter.SessionCustomAdapter;
import com.tech.eskool.database.MyDatabase;
import com.tech.eskool.databinding.ActivityTeacherDashBoardBinding;
import com.tech.eskool.dto.MenuResponse;
import com.tech.eskool.dto.SessionResponse;
import com.tech.eskool.service.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TeacherDashBoardActivity extends AppCompatActivity {

    AppBarConfiguration mAppBarConfiguration;
    SessionManager sessionManager;
    NavigationView navigationView;
    NavController navController;
    MyDatabase myDatabase;
    DrawerLayout drawer;
    CircularImageView imageView;
    Spinner spinner_session;
    ArrayList<SessionResponse> sessionResponseArrayList;
    SessionCustomAdapter sessionCustomAdapter;
    TextView name, designation;
    ExpandableListView multiNav;
    CustomNavigationViewAdapter customNavigationViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        myDatabase = new MyDatabase(this, null, null, 0);
        sessionResponseArrayList = myDatabase.getSessionData();
        sessionManager.prepareDashboardStart(getIntent(), savedInstanceState == null);
        ActivityTeacherDashBoardBinding binding = ActivityTeacherDashBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarTeacherDashBoard.toolbar);

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        multiNav = binding.multiNav;
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_menu)
                .setOpenableLayout(drawer)
                .build();
        name = (TextView) navigationView.findViewById(R.id.name);
        designation = (TextView) navigationView.findViewById(R.id.designation);
        imageView = navigationView.findViewById(R.id.imageView);

        Map<String, String> user = sessionManager.getUserDetails();
        String userName = user.get(SessionManager.USERNAME) != null ? user.get(SessionManager.USERNAME) : "";
        String empId = user.get(SessionManager.EMPID);
        // Used to print the user name twice: "John(John)".
        name.setText(empId != null && !empId.isEmpty() ? userName + " (" + empId + ")" : userName);
        String empType = user.get(SessionManager.EMPTYPE);
        designation.setText("Designation : " + (empType != null ? empType : "-"));
        String photo = user.get(SessionManager.PHOTO);
        if (photo != null && !photo.trim().isEmpty()) {
            Picasso.get().load(photo).placeholder(R.drawable.ic_teacher).error(R.drawable.ic_teacher).into(imageView);
        }

        spinner_session = navigationView.findViewById(R.id.session);

        sessionCustomAdapter = new SessionCustomAdapter(this, sessionResponseArrayList);
        spinner_session.setAdapter(sessionCustomAdapter);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_teacher_dash_board);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        getMenuItem();
        // Show the session that is actually in use, instead of always the first one.
        String savedSession = sessionManager.getSessionId();
        for (int i = 0; i < sessionResponseArrayList.size(); i++) {
            if (sessionResponseArrayList.get(i).getSessionId() != null
                    && sessionResponseArrayList.get(i).getSessionId().equals(savedSession)) {
                spinner_session.setSelection(i, false);
                break;
            }
        }
        spinner_session.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SessionResponse selected = sessionResponseArrayList.get(position);
                String sessionId = selected.getSessionId();
                // Spinner also fires once on start-up; only react to a real change.
                if (sessionId == null || sessionId.equals(sessionManager.getSessionId())) return;
                sessionManager.setSession(sessionId);
                Toast.makeText(TeacherDashBoardActivity.this, "Selected session: " + selected.getSessionName(), Toast.LENGTH_SHORT).show();
                // Reload the open page so it uses the new session.
                navController.navigate(R.id.nav_menu, null,
                        new NavOptions.Builder().setPopUpTo(R.id.nav_menu, true).build());
                drawer.closeDrawer(GravityCompat.START);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.students_dashboard, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_teacher_dash_board);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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
                    Log.i("SESSION-", sessionManager.getMenuUrl()+"");
                    navController.navigate(R.id.nav_menu);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            });
        }
    }
}