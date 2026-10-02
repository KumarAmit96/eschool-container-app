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
        try {
            Intent intent = getIntent();
            if (!Objects.requireNonNull(intent.getStringExtra("MESSAGE")).isEmpty()) {
                sessionManager.getNotificationAlertWithOk(intent.getStringExtra("TITLE"), intent.getStringExtra("MESSAGE"));
            }
        } catch (Exception e) {
            Log.i("NOTIFICATION_TEA", "ERROR ::"+e.getLocalizedMessage());
        }
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

        name.setText(sessionManager.getUserDetails().get(SessionManager.USERNAME).concat("(").concat(sessionManager.getUserDetails().get(SessionManager.USERNAME)).concat(")") );
        designation.setText("Designation : ".concat(sessionManager.getUserDetails().get(SessionManager.EMPTYPE)));
        Picasso.get().load(sessionManager.getUserDetails().get(SessionManager.PHOTO)).error(R.drawable.ic_teacher).into(imageView);

        spinner_session = navigationView.findViewById(R.id.session);

        sessionCustomAdapter = new SessionCustomAdapter(this, sessionResponseArrayList);
        spinner_session.setAdapter(sessionCustomAdapter);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_teacher_dash_board);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        getMenuItem();
        try {
            spinner_session.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        String tag = (String) view.getTag();
                        sessionManager.setSession(tag);
                        Toast.makeText(TeacherDashBoardActivity.this, "Selected session: "+sessionResponseArrayList.get(position).getSessionName(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(TeacherDashBoardActivity.this, "Error::"+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch(Exception e) {
            Log.i("SPINNER", "NO ITEM :"+e.getLocalizedMessage());
        }
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