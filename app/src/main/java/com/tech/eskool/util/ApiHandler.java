package com.tech.eskool.util;

import com.tech.eskool.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiHandler {

    private final static String TAG = "ApiHandler";
    private final SessionManager sessionManager;
    private final MyDatabase myDatabase;

    public ApiHandler(SessionManager sessionManager, MyDatabase myDatabase) {
        this.sessionManager = sessionManager;
        this.myDatabase = myDatabase;
    }

    public void setStudentMenuItem(String collegeCode, String token) {
        setStudentMenuItem(collegeCode, token, null);
    }

    /** Loads the student menu into the DB; {@code onDone} runs on the main thread when finished (success or not). */
    public void setStudentMenuItem(String collegeCode, String token, @Nullable Runnable onDone) {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        api_interface.getMenuForStudent(token, collegeCode).enqueue(new MenuCallback(onDone));
    }

    public void setUserMenuItem(String collegeCode, String token) {
        setUserMenuItem(collegeCode, token, null);
    }

    /** Loads the employee menu into the DB; {@code onDone} runs on the main thread when finished (success or not). */
    public void setUserMenuItem(String collegeCode, String token, @Nullable Runnable onDone) {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        api_interface.getMenuForUser(token, collegeCode).enqueue(new MenuCallback(onDone));
    }

    private class MenuCallback implements Callback<MenuDto> {
        private final Runnable onDone;

        MenuCallback(@Nullable Runnable onDone) {
            this.onDone = onDone;
        }

        @Override
        public void onResponse(@NonNull Call<MenuDto> call, @NonNull Response<MenuDto> response) {
            try {
                MenuDto body = response.body();
                if (body != null && Boolean.TRUE.equals(body.getStatus()) && body.getMenuResponse() != null) {
                    saveMenu(body.getMenuResponse());
                } else if (body != null) {
                    // Server rejected the token: session expired.
                    Log.w(TAG, "Menu: status false: " + body.getMessage());
                    sessionManager.getSessionLogout();
                } else {
                    Log.w(TAG, "Menu: HTTP " + response.code() + " " + response.message());
                }
            } catch (Exception e) {
                Log.e(TAG, "Menu: error handling response", e);
            }
            done();
        }

        @Override
        public void onFailure(@NonNull Call<MenuDto> call, @NonNull Throwable t) {
            Log.e(TAG, "Menu: request failed", t);
            done();
        }

        private void done() {
            if (onDone != null) onDone.run();
        }
    }

    private void saveMenu(List<MenuResponse> menu) {
        int id = 1;
        myDatabase.deleteMenuItem();
        Map<Integer, List<MenuResponse>> childMenu = new LinkedHashMap<>();
        for (MenuResponse item : menu) {
            item.setId(id);
            if (item.getSubmenu() != null && !item.getSubmenu().isEmpty()) {
                childMenu.put(id, item.getSubmenu());
            }
            myDatabase.setMenuItem(item, 0);
            id++;
        }
        for (Map.Entry<Integer, List<MenuResponse>> entry : childMenu.entrySet()) {
            for (MenuResponse child : entry.getValue()) {
                child.setId(id++);
                myDatabase.setMenuItem(child, entry.getKey());
            }
        }
    }

    public void setSibling(String token, String sessionId, String collegeCode) {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        Call<StudentSiblingRecord> call = api_interface.getSiblingData(token, sessionId, collegeCode);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<StudentSiblingRecord> call, @NonNull Response<StudentSiblingRecord> response) {
                try {
                    StudentSiblingRecord body = response.body();
                    if (body == null) {
                        Log.w(TAG, "Sibling: HTTP " + response.code() + " " + response.message());
                    } else if (!Boolean.TRUE.equals(body.getStatus())) {
                        Log.w(TAG, "Sibling: status false: " + body.getMessage());
                        sessionManager.getSessionLogout();
                    } else if (body.getResponse() != null) {
                        // Replace instead of append: this runs on every app start.
                        myDatabase.deleteSiblingData();
                        for (StudentLoginResponse student : body.getResponse()) {
                            myDatabase.setDataIntoSiblingTable(student);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Sibling: error handling response", e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StudentSiblingRecord> call, @NonNull Throwable t) {
                Log.e(TAG, "Sibling: request failed", t);
            }
        });
    }

    public void setSessionData(String collegeCode) {
        Api_Interface api_interface = ApiClient.getClient().create(Api_Interface.class);
        Call<SessionClass> call = api_interface.getSessionData(collegeCode);
        call.enqueue(new Callback<SessionClass>() {
            @Override
            public void onResponse(@NonNull Call<SessionClass> call, @NonNull Response<SessionClass> response) {
                try {
                    SessionClass body = response.body();
                    if (body == null) {
                        Log.d(TAG, "Session: HTTP " + response.code() + " " + response.message());
                    } else if (!Boolean.TRUE.equals(body.getStatus()) || body.getResponse() == null || body.getResponse().isEmpty()) {
                        Log.d(TAG, "Session: " + body.getMessage());
                    } else {
                        myDatabase.deleteSessionData();
                        sessionManager.setSession(body.getResponse().get(0).getSessionId());
                        for (SessionResponse sessionResponse : body.getResponse()) {
                            myDatabase.setSessionData(sessionResponse);
                        }
                    }
                } catch (Exception t) {
                    Log.e(TAG, "Session: error handling response", t);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessionClass> call, @NonNull Throwable t) {
                Log.e(TAG, "Session: request failed", t);
            }
        });
    }

}
