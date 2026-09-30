package com.tech.eskool.service;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tech.eskool.R;
import com.tech.eskool.StartActivity;
import com.tech.eskool.StudentsDashboardActivity;
import com.tech.eskool.TeacherDashBoardActivity;

import java.util.HashMap;

public class SessionManager {

    //SharedPreference's part
    private static final String MYPREF = "my_pref";
    private final Context context;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    //User Employee/Admin Login Credential
    public static final String USERID = "user_id";
    public static final String USERNAME = "username";
    public static final String EMPID = "emp_id";
    public static final String EMPTYPE = "emp_type";
    public static final String CLASSID = "class_id";
    public static final String SECTIONID = "section_id";
    public static final String TOKEN = "token";
    public static final String APP_TOKEN = "app_token";
    public static final String SESSIONID = "session_id";
    public static final String IS_LOGIN = "IsLoggedIn";
    public static final String PHOTO = "emp_photo";
    public static final String COLLEGE_NAME = "college_name";
    public static final String PAYMENT_STATUS = "payment_status";
    public static final String ENROLLMENT_NO = "enrollment_no";
    public static final String LANDING_PAGE = "landing_page";
    public static final String MENU_PAGE = "menu_page";
    public static final String FCM = "fcm_token";

    public static final String YOUTUBE_URL = "youtube";
    public static final String CHANGE_PASSWORD = "change_password";
    public static final String NOTIFICATION = "notification";



    //Student Login Credential
    public static final String STUDENT_ID = "student_id";
    public static final String STUDENT_NAME = "student_name";
    public static final String STUDENT_CLASS = "student_class_id";
    public static final String STUDENT_SECTION = "student_section_id";
    public static final String STUDENT_TOKEN = "student_token";
    public static final String STUDENT_PHOTO = "photo";
    public static final String STUDENT_APP_TOKEN = "student_app_token";

    //College code
    public static final String COLLEGE_CODE = "college_code";

    public static final String BLOB_FILE = "blob_file";



    public SessionManager(Context context)
    {
        this.context = context;
        preferences = context.getSharedPreferences(MYPREF, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    // Student Login Details
    public void setStudentLoginCredential(String student_id, String student_name, String Classid,
                                          String Sectionid, String Token, String photo, String StudentSession,
                                          String app_token, String code, String college_name,
                                          String payment_status, String enrollment_no, String landingPage,
                                          String changePassword, String notification)
    {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(STUDENT_ID, student_id);
        editor.putString(STUDENT_NAME, student_name);
        editor.putString(STUDENT_PHOTO, photo);
        editor.putString(STUDENT_CLASS, Classid);
        editor.putString(STUDENT_SECTION, Sectionid);
        editor.putString(STUDENT_TOKEN, Token);
        editor.putString(SESSIONID, StudentSession);
        editor.putString(STUDENT_APP_TOKEN, app_token);
        editor.putString(COLLEGE_CODE, code);
        editor.putString(COLLEGE_NAME, college_name);
        editor.putString(PAYMENT_STATUS, payment_status);
        editor.putString(ENROLLMENT_NO, enrollment_no);
        editor.putString(LANDING_PAGE, landingPage);
        editor.putString(MENU_PAGE, null);
        editor.putString(NOTIFICATION, notification);
        editor.putString(CHANGE_PASSWORD, changePassword);
        editor.commit();
    }

    //Employee Login Credential
    public void  setLoginCredential(String Userid, String Username, String Empid, String Emptype, String Classid,
                                    String Sectionid, String Token, String app_token, String code, String photo,
                                    String college_name, String landingPage, String changePassword, String notification)
    {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(USERID, Userid);
        editor.putString(USERNAME, Username);
        editor.putString(EMPID, Empid);
        editor.putString(EMPTYPE, Emptype);
        editor.putString(CLASSID, Classid);
        editor.putString(SECTIONID, Sectionid);
        editor.putString(TOKEN, Token);
        editor.putString(APP_TOKEN, app_token);
        editor.putString(COLLEGE_CODE, code);
        editor.putString(PHOTO, photo);
        editor.putString(COLLEGE_NAME, college_name);
        editor.putString(LANDING_PAGE, landingPage);
        editor.putString(MENU_PAGE, null);
        editor.putString(NOTIFICATION, notification);
        editor.putString(CHANGE_PASSWORD, changePassword);
        editor.commit();
    }

    //Get User Info
    public HashMap<String, String> getUserDetails(){

        HashMap<String, String> user = new HashMap<String, String>();
        user.put(USERID, preferences.getString(USERID, null));
        user.put(USERNAME, preferences.getString(USERNAME, null));
        user.put(EMPID, preferences.getString(EMPID, null));
        user.put(EMPTYPE, preferences.getString(EMPTYPE, null));
        user.put(CLASSID, preferences.getString(CLASSID, null));
        user.put(SECTIONID, preferences.getString(SECTIONID, null));
        user.put(TOKEN, preferences.getString(TOKEN, null));
        user.put(APP_TOKEN, preferences.getString(APP_TOKEN, null));
        user.put(PHOTO, preferences.getString(PHOTO, null));
        user.put(LANDING_PAGE, preferences.getString(LANDING_PAGE, null));
        user.put(MENU_PAGE, preferences.getString(MENU_PAGE, null));
        user.put(CHANGE_PASSWORD, preferences.getString(CHANGE_PASSWORD, null));
        user.put(NOTIFICATION, preferences.getString(NOTIFICATION, null));
        // return user
        return user;
    }

    //Get Student Info
    public HashMap<String, String> getStudentDetails(){

        HashMap<String, String> user = new HashMap<String, String>();
        user.put(STUDENT_ID, preferences.getString(STUDENT_ID, null));
        user.put(STUDENT_NAME, preferences.getString(STUDENT_NAME, null));
        user.put(STUDENT_PHOTO, preferences.getString(STUDENT_PHOTO, null));
        user.put(STUDENT_CLASS, preferences.getString(STUDENT_CLASS, null));
        user.put(STUDENT_SECTION, preferences.getString(STUDENT_SECTION, null));
        user.put(STUDENT_TOKEN, preferences.getString(STUDENT_TOKEN, null));
        user.put(SESSIONID, preferences.getString(SESSIONID, null));
        user.put(STUDENT_APP_TOKEN, preferences.getString(STUDENT_APP_TOKEN, null));
        user.put(PAYMENT_STATUS, preferences.getString(PAYMENT_STATUS, null));
        user.put(LANDING_PAGE, preferences.getString(LANDING_PAGE, null));
        user.put(MENU_PAGE, preferences.getString(MENU_PAGE, null));
        user.put(CHANGE_PASSWORD, preferences.getString(CHANGE_PASSWORD, null));
        user.put(NOTIFICATION, preferences.getString(NOTIFICATION, null));
        // return user
        return user;
    }

    //Get Logout
    public void logoutUser()
    {
        editor.clear();
        editor.commit();

        Intent i = new Intent(context, StartActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        context.startActivity(i);
    }

    public void exit()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.myAlertDialog);
        alert.setTitle(R.string.app_name);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage("Do you want to Exit?");
        alert.setCancelable(false);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(context instanceof StudentsDashboardActivity){
                    ((StudentsDashboardActivity) context).finish();
                }else{
                    ((TeacherDashBoardActivity) context).finish();
                }
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
    }

    public void cleanSessionData()
    {
        String getAppToken = getStudentAppToken();
        editor.clear();
        editor.commit();
        FirebaseMessaging.getInstance().unsubscribeFromTopic(getAppToken).addOnSuccessListener(((StudentsDashboardActivity)context), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("FIREBASE SERVICE", "ID UNSUBSCRIBE SUCCESSFULLY.....");
            }
        });

    }

    public void getSiblingEntry()
    {
        Intent i = new Intent(context, StartActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        context.startActivity(i);
    }

    // Get Login State
    public boolean isLoggedIn()
    {
        return preferences.getBoolean(IS_LOGIN, false);
    }

    public void setBlobFile(String url)
    {
        editor.putString(BLOB_FILE, url);
        editor.commit();
    }

    public String getBlobFile()
    {
        return preferences.getString(BLOB_FILE, null);
    }

    //set first session data
    public void setSession(String id)
    {
        editor.putString(SESSIONID, id);
        editor.commit();
    }

    public void setFcmToken(String token)
    {
        editor.putString(FCM, token);
        editor.commit();
    }

    public void setYoutubeUrl(String url)
    {
        editor.putString(YOUTUBE_URL, url);
        editor.commit();
    }

    public void setLandingUrl(String url)
    {
        editor.putString(LANDING_PAGE, url);
        editor.commit();
    }

    public String getYoutubeUrl()
    {
        return preferences.getString(YOUTUBE_URL, null);
    }

    public void setNotification(String url)
    {
        editor.putString(NOTIFICATION, url);
        editor.commit();
    }

    public String getNotification()
    {
        return preferences.getString(NOTIFICATION, null);
    }

    public void setChangePassword(String url)
    {
        editor.putString(CHANGE_PASSWORD, url);
        editor.commit();
    }

    public String getChangePassword()
    {
        return preferences.getString(CHANGE_PASSWORD, null);
    }
    public String getLandingUrl(){
        return preferences.getString(LANDING_PAGE, null);
    }

    public String getFcmToken(){
        return preferences.getString(FCM, null);
    }


    public void setMenuUrl(String url)
    {
        editor.putString(MENU_PAGE, url);
        editor.commit();
    }

    public String getMenuUrl(){
        return preferences.getString(MENU_PAGE, null);
    }

    public String getSessionId()
    {
        return preferences.getString(SESSIONID, null);
    }

    public String getAppToken()
    {
        return preferences.getString(APP_TOKEN, null);
    }

    public String getStudentAppToken()
    {
        return preferences.getString(STUDENT_APP_TOKEN, null);
    }

    public String getCollegeName()
    {
        return preferences.getString(COLLEGE_NAME, null);
    }

    public String getPaymentStatus()
    {
        return preferences.getString(PAYMENT_STATUS, null);
    }

    public String getEnrollmentNo(){ return  preferences.getString(ENROLLMENT_NO, null);}

    public String getUserToken(){ return  preferences.getString(TOKEN, null);}

    public String getStudentToken(){ return  preferences.getString(STUDENT_TOKEN, null);}


    public  void getSessionLogout()
    {
        final String appToken = getAppToken();
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.myAlertDialog);
        alert.setTitle(R.string.app_name);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage("Do you want to Log-out?");
        alert.setCancelable(false);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(context instanceof TeacherDashBoardActivity) {

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(appToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.i("FIREBASE SERVICE", "ID UNSUBSCRIBE SUCCESSFULLY.....");
                            logoutUser();
                            ((TeacherDashBoardActivity) context).finish();
                        }
                    });

                } else if(context instanceof StudentsDashboardActivity) {

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(appToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.i("FIREBASE SERVICE", "ID UNSUBSCRIBE SUCCESSFULLY.....");
                            logoutUser();
                            ((StudentsDashboardActivity) context).finish();
                        }
                    });
                } else {
                    Log.i("FIREBASE SERVICE", "NOTHING TO UNSUBSCRIBE SUCCESSFULLY.....");
                }
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
    }

    public String getCollegeCode()
    {
        return preferences.getString(COLLEGE_CODE, null);
    }

    public void getAlertWithOk(String msg)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.myAlertDialog);
        alert.setTitle(R.string.app_name);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }

    public void getNotificationAlertWithOk(String title, String msg)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.myAlertDialog);
        alert.setTitle(title);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }


    public boolean checkConnectivity(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());

        } catch (NullPointerException e) {
            return false;
        }
    }

    public void showDownloadCompleteDialog(Uri uri) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.myAlertDialog);
        alert.setTitle(R.string.app_name);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setTitle("Download Complete!");
        alert.setMessage("Do you want to open it?");
        alert.setCancelable(false);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
                dialog.dismiss();
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

    }

}
