package com.tech.eskool.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.tech.eskool.database.MyDatabase;
import com.tech.eskool.dto.MenuDto;
import com.tech.eskool.dto.MenuResponse;
import com.tech.eskool.dto.SessionClass;
import com.tech.eskool.dto.SessionResponse;
import com.tech.eskool.dto.StudentLoginResponse;
import com.tech.eskool.dto.StudentSiblingRecord;
import com.tech.eskool.rest_service.ApiClient;
import com.tech.eskool.rest_service.Api_Interface;
import com.tech.eskool.service.SessionManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiHandler {

    private final static String TAG = ApiHandler.class.toString();
    private final SessionManager sessionManager;
    private final MyDatabase myDatabase;

    public ApiHandler(SessionManager sessionManager, MyDatabase myDatabase) {
        this.sessionManager = sessionManager;
        this.myDatabase = myDatabase;
    }

    public void setStudentMenuItem(String collegeCode, String token)
    {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        Call<MenuDto> call = api_interface.getMenuForStudent(token, collegeCode);
        call.enqueue(new Callback<MenuDto>() {
            @Override
            public void onResponse(@NonNull Call<MenuDto> call, @NonNull Response<MenuDto> response) {
                try {
                    if(response.body() != null ? response.body().getStatus() : false) {
                        int id = 1;
                        myDatabase.deleteMenuItem();
                        Map<Integer, List<MenuResponse>> childMenu = new LinkedHashMap<>();
                        for (MenuResponse item : response.body().getMenuResponse()) {
                            item.setId(id);
                            if(item.getSubmenu() != null && !item.getSubmenu().isEmpty()){
                                childMenu.put(id, item.getSubmenu());
                            }
                            myDatabase.setMenuItem(item, 0);
                            id++;
                        }
                        if(!childMenu.isEmpty()){
                            AtomicInteger childId = new AtomicInteger(id);
                            childMenu.forEach((k,v) -> {
                                v.forEach(x -> {
                                    x.setId(childId.get());
                                    myDatabase.setMenuItem(x, k);
                                    childId.set(childId.get()+1);
                                });
                            });
                        }
                    } else {
                        Log.i(TAG, response.toString());
                        sessionManager.getSessionLogout();
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MenuDto> call, @NonNull Throwable t) {
                Log.i(TAG, Objects.requireNonNull(t.getLocalizedMessage()));
            }
        });
    }

    public void setUserMenuItem(String collegeCode, String token)
    {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        Call<MenuDto> call = api_interface.getMenuForUser(token, collegeCode);
        call.enqueue(new Callback<MenuDto>() {
            @Override
            public void onResponse(@NonNull Call<MenuDto> call, @NonNull Response<MenuDto> response) {
                try {
                    if(response.body() != null ? response.body().getStatus() : false) {
                        int id = 1;
                        myDatabase.deleteMenuItem();
                        Map<Integer, List<MenuResponse>> childMenu = new LinkedHashMap<>();
                        for (MenuResponse item : response.body().getMenuResponse()) {
                            item.setId(id);
                            if(item.getSubmenu() != null && !item.getSubmenu().isEmpty()){
                                childMenu.put(id, item.getSubmenu());
                            }
                            myDatabase.setMenuItem(item, 0);
                            id++;
                        }
                        if(!childMenu.isEmpty()){
                            AtomicInteger childId = new AtomicInteger(id);
                            childMenu.forEach((k,v) -> {
                                v.forEach(x -> {
                                    x.setId(childId.get());
                                    myDatabase.setMenuItem(x, k);
                                    childId.set(childId.get()+1);
                                });
                            });
                        }
                    } else {
                        Log.i(TAG, response.toString());
                        sessionManager.getSessionLogout();
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MenuDto> call, @NonNull Throwable t) {
                Log.i(TAG, Objects.requireNonNull(t.getLocalizedMessage()));
            }
        });
    }

    public void setSibling(String token, String sessionId, String collegeCode) {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        Call<StudentSiblingRecord> call = api_interface.getSiblingData(token, sessionId, collegeCode);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<StudentSiblingRecord> call, @NonNull Response<StudentSiblingRecord> response) {
                try {
                    if (response.body() != null) {
                        if (response.body().getStatus() && !response.body().getResponse().isEmpty()) {
                            for (StudentLoginResponse student : response.body().getResponse()) {
                                myDatabase.setDataIntoSiblingTable(student);
                            }
                        } else {
                            sessionManager.getSessionLogout();
                            Log.i(TAG, response.body().getMessage());
                        }
                    } else {
                        Log.i(TAG, response.toString());
                    }
                } catch (Exception e) {
                    Log.i(TAG, response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StudentSiblingRecord> call, Throwable t) {
                Log.i(TAG, Objects.requireNonNull(t.getLocalizedMessage()));

            }
        });
    }

    public void setSessionData(String collegeCode)
    {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        Call<SessionClass> call = api_interface.getSessionData(collegeCode);
        call.enqueue(new Callback<SessionClass>() {
            @Override
            public void onResponse(@NonNull Call<SessionClass> call, @NonNull Response<SessionClass> response) {
                try {
                    if(response.body()!=null) {
                        if(response.body().getStatus()) {
                            myDatabase.deleteSessionData();
                            sessionManager.setSession(response.body().getResponse().get(0).getSessionId());
                            for(SessionResponse sessionResponse : response.body().getResponse() )
                                myDatabase.setSessionData(sessionResponse);

                        } else {
                            Log.d(TAG, response.body().getMessage());
                        }
                    } else {
                        Log.d(TAG, response.message());
                    }
                } catch (Exception t) {
                    Log.d(TAG, Objects.requireNonNull(t.getMessage()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessionClass> call, @NonNull Throwable t) {
                Log.d(TAG, Objects.requireNonNull(t.getMessage()));
            }
        });
    }

}
