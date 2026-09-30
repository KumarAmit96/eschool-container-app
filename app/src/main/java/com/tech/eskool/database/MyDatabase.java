package com.tech.eskool.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tech.eskool.dto.MenuResponse;
import com.tech.eskool.dto.SessionResponse;
import com.tech.eskool.dto.StudentLoginResponse;

import java.util.ArrayList;
import java.util.List;


public class MyDatabase extends SQLiteOpenHelper {

    //DATABASE DEFINITION
    private static final String DB_NAME = "my_school.db";
    private static final int DB_VERSION = 2;

    // Table Name
    public static final String MENU_ITEM = "MENU_ITEM";
    public static final String CLASS_SECTION_TABLE = "CLASS_SECTION";
    public static final String SESSION_DATA = "SESSION_DATA";
    public static final String TEACHER_DATA = "TEACHER_DATA";
    public static final String GRAPHICAL_ATTENDENCE = "GRAPHICAL_ATTENDENCE";
    public static final String STUDENT_BIRTHDAY = "STUDENT_BIRTHDAY";
    public static final String TEACHER_BIRTHDAY = "TEACHER_BIRTHDAY";
    public static final String HOUSE_CLASS = "HOUSE_CLASS";
    public static final String EMPLOYEE_TYPE = "EMPLOYEE_TYPE";
    public static final String DEPARTMENT_DETAIL = "DEPARTMENT_DETAIL";
    public static final String STATE_LIST = "STATE_LIST";
    public static final String FEES_TYPE_LIST = "FEES_TYPE_LIST";
    public static final String BLOOD_GROUP_LIST = "BLOOD_GROUP_LIST";
    public static final String VEHICLE_LIST = "VEHICLE_LIST";
    public static final String ROUTE_LIST = "ROUTE_LIST";
    public static final String RELIGION_LIST = "RELIGION_LIST";
    public static final String CATEGORY_LIST = "CATEGORY_LIST";
    public static final String TIME_TABLE = "TIME_TABLE";
    public static final String CLASSWORK_TABLE = "CLASS_RECORD";
    public static final String MY_SIBLING_TABLE = "MY_SIBLING_TABLE";


    // Table columns for homework table
    public static final String HOMEWORKID = "hw_id";
    public static final String CLASSID = "class_id";
    public static final String SECTIONID = "section_id";
    public static final String TEACHERID = "teacher_id";
    public static final String HEADING = "heading";
    public static final String MESSAGE = "message";
    public static final String DATETIME = "date_time";
    public static final String HOMEWORKPHOTO = "hw_photo";

    // Table columns for classwork table
    public static final String CLASSWORKID = "hw_ids";
    public static final String CLASSIDS = "class_ids";
    public static final String SECTIONIDS = "section_ids";
    public static final String TEACHERIDS = "teacher_ids";
    public static final String HEADINGS = "headings";
    public static final String MESSAGES = "messages";
    public static final String DATETIMES = "date_times";
    public static final String HOMEWORKPHOTOS = "hw_photos";

    //Table columns for class and section
    public static final String CLASS_ID = "class_id";
    public static final String SECTION_ID = "section_id";
    public static final String CLASSNAME = "class_name";
    public static final String SECTIONNAME = "section_name";

    //Table columns for session data
    public static final String SESSION_ID = "session_id";
    public static final String SESSION_NAME = "session_name";

    //Table columns for teacher data
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String EMPLOYEE_NAME = "employee_name";
    public static final String EMPLOYEE_PHOTO = "employee_photo";


    //Table columns for Graph dataset
    public static final String PRESENT = "present";
    public static final String ABSENT = "absent";
    public static final String HALFDAY = "halfday";
    public static final String LEAVE = "leave";
    public static final String MONTH = "month";

    //Table columns for Student birthday
    public static final String BSTUDENT_NAME = "bstudent_name";
    public static final String BSTUDENT_CLASS = "bstudent_class";
    public static final String BSTUDENT_SECTION = "bstudent_section";
    public static final String BSTUDENT_PHOTO = "bstudent_photo";

    //Table columns for Teacher birthday
    public static final String BTEACHER_NAME = "bteacher_name";
    public static final String BTEACHER_DESIGNATION = "bteacher_designation";
    public static final String BTEACHER_PHOTO = "bstudent_photo";

    //Table columns for House Detail
    public static final String HOUSE_ID = "house_id";
    public static final String HOUSE_NAME = "house_name";

    //Table columns for Employee Details
    public static final String EMPLOYMENT_ID = "employment_id";
    public static final String EMPLOYMENT_TYPE = "employment_type";

    //Table columns for Department Details
    public static final String DEPARTMENT_ID = "department_id";
    public static final String DEPARTMENT_NAME = "department_name";
    public static final String DEPARTMENT_SNAME = "department_sname";

    //Table columns for State List
    public static final String STATE_ID = "state_id";
    public static final String STATE_NAME = "state_name";

    //Table columns for Time Table
    public static final String CLASS_NAME = "class_name";
    public static final String SECTION_NAME = "section_name";
    public static final String SUBJECT_NAME = "subject_name";
    public static final String TEACHER_NAME = "teacher_name";
    public static final String PERIOD_NAME = "period_name";
    public static final String PERIOD_TIME = "period_time";
    public static final String DAYS_NAME = "days_name";

    //Tables columns for Sibling tables
    public static final String SIBLING_SESSION_ID = "sib_session_id";
    public static final String SIBLING_STUDENT_ID = "sib_student_id";
    public static final String SIBLING_STUDENT_NAME = "sib_student_name";
    public static final String SIBLING_CLASS = "sib_class_id";
    public static final String SIBLING_SECTION = "sib_section_id";
    public static final String SIBLING_TOKEN = "sib_token";
    public static final String SIBLING_APP_TOKEN = "sib_app_token";
    public static final String SIBLING_PHOTO = "sib_photo";
    public static final String SIBLING_LANDING_PAGE = "sib_landing_page";


    //Table menu item
    public static final String MENU_ID = "menu_id";
    public static final String MENU_NAME = "menu_name";
    public static final String MENU_URL = "menu_url";
    public static final String MENU_PARENT_ID = "menu_parent_id";
    public static final String MENU_LARGE_ICON = "menu_large_icon";
    public static final String MENU_SMALL_ICON = "menu_small_icon";

    // Creating table query
    private static final String CREATE_TABLE_FOR_MENU = "create table " + MENU_ITEM +  "(" +MENU_NAME
            + " TEXT, " + MENU_URL + " TEXT, " + MENU_ID +" INTEGER, "+MENU_PARENT_ID +" INTEGER, "+ MENU_LARGE_ICON + " TEXT, "+ MENU_SMALL_ICON +" TEXT );";

    private static final String CREATE_TABLE_FOR_SIBLING = "create table " + MY_SIBLING_TABLE + "(" +SIBLING_SESSION_ID
            + " TEXT, " + SIBLING_STUDENT_ID + " TEXT, " + SIBLING_STUDENT_NAME + " TEXT, "+ SIBLING_CLASS + " TEXT, "
            + SIBLING_SECTION +" TEXT, "+ SIBLING_TOKEN +" TEXT, "+ SIBLING_APP_TOKEN +" TEXT, " + SIBLING_LANDING_PAGE +" TEXT, " + SIBLING_PHOTO +" TEXT );";

    private static final String CREATE_TABLE_FOR_SESSION_DATA = "create table " + SESSION_DATA + "(" +SESSION_ID
            + " TEXT, " + SESSION_NAME +" TEXT );";
    public MyDatabase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FOR_MENU);
        db.execSQL(CREATE_TABLE_FOR_SIBLING);
        db.execSQL(CREATE_TABLE_FOR_SESSION_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MENU_ITEM);
        db.execSQL("DROP TABLE IF EXISTS " + MY_SIBLING_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SESSION_DATA);
        onCreate(db);
    }


    //set teacher data
    public void setMenuItem(MenuResponse response, Integer parentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MENU_ID, response.getId());
        values.put(MENU_NAME, response.getName());
        values.put(MENU_URL, response.getUrl());
        values.put(MENU_LARGE_ICON, response.getIconLarge());
        values.put(MENU_SMALL_ICON, response.getIconSmall());
        values.put(MENU_PARENT_ID, parentId);
        // insert row
        long i = db.insert(MENU_ITEM, null, values);
        Log.i("DB", i+"::"+response);
        db.close();
    }



    //read homework by admin
    @SuppressLint("Range")
    public String getMenuUrlByName(String menuName)
    {
        String selectQuery = "SELECT  * FROM " + MENU_ITEM + " WHERE " +MENU_NAME+ " = '"+ menuName + "' ";
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) return cursor.getString(cursor.getColumnIndex(MENU_NAME)).concat("::").concat(cursor.getString(cursor.getColumnIndex(MENU_URL)));
        db.close();
        return null;
    }

    //read homework by admin
    @SuppressLint("Range")
    public String getNameByUrl(String menuUrl)
    {
        String selectQuery = "SELECT  * FROM " + MENU_ITEM + " WHERE " +MENU_URL+ " = '"+ menuUrl + "' ";
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) return cursor.getString(cursor.getColumnIndex(MENU_NAME));
        db.close();
        return null;
    }

    @SuppressLint("Range")
    public List<MenuResponse> getMenuItem()
    {
        ArrayList<MenuResponse> menuList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + MENU_ITEM ;
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MenuResponse data = new MenuResponse();
                data.setName(cursor.getString(cursor.getColumnIndex(MENU_NAME)));
                data.setId(cursor.getInt(cursor.getColumnIndex(MENU_ID)));
                data.setUrl(cursor.getString(cursor.getColumnIndex(MENU_URL)));
                data.setIconLarge(cursor.getString(cursor.getColumnIndex(MENU_LARGE_ICON)));
                data.setIconSmall(cursor.getString(cursor.getColumnIndex(MENU_SMALL_ICON)));
                data.setParentId(cursor.getInt(cursor.getColumnIndex(MENU_PARENT_ID)));
                menuList.add(data);

            } while (cursor.moveToNext());
        }
        db.close();
        return menuList;
    }

    //set homework data
    public void setDataIntoSiblingTable(StudentLoginResponse student) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(SIBLING_SESSION_ID, student.getSessionId());
        values.put(SIBLING_STUDENT_ID, student.getStudentId());
        values.put(SIBLING_STUDENT_NAME, student.getStudentName());
        values.put(SIBLING_CLASS, student.getClassId());
        values.put(SIBLING_SECTION, student.getSectionId());
        values.put(SIBLING_TOKEN, student.getToken());
        values.put(SIBLING_APP_TOKEN, student.getAppToken());
        values.put(SIBLING_PHOTO, student.getPhoto());
        values.put(SIBLING_LANDING_PAGE, student.getLandingPage());
        Log.i("SIBLING", "RECORD");
        // insert row
        long id = db.insert(MY_SIBLING_TABLE, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public StudentLoginResponse getSiblingData(String studentId)
    {
        StudentLoginResponse data = new StudentLoginResponse();
        String selectQuery = "SELECT  * FROM " + MY_SIBLING_TABLE + " WHERE " +SIBLING_STUDENT_ID+ " = "+ studentId;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                data.setSessionId(cursor.getString(cursor.getColumnIndex(SIBLING_SESSION_ID)));
                data.setStudentId(cursor.getString(cursor.getColumnIndex(SIBLING_STUDENT_ID)));
                data.setStudentName(cursor.getString(cursor.getColumnIndex(SIBLING_STUDENT_NAME)));
                data.setClassId(cursor.getString(cursor.getColumnIndex(SIBLING_CLASS)));
                data.setSectionId(cursor.getString(cursor.getColumnIndex(SIBLING_SECTION)));
                data.setToken(cursor.getString(cursor.getColumnIndex(SIBLING_TOKEN)));
                data.setAppToken(cursor.getString(cursor.getColumnIndex(SIBLING_APP_TOKEN)));
                data.setPhoto(cursor.getString(cursor.getColumnIndex(SIBLING_PHOTO)));
                data.setLandingPage(cursor.getString(cursor.getColumnIndex(SIBLING_LANDING_PAGE)));

            } while (cursor.moveToNext());
        }
        db.close();
        // return sibling data
        return data;
    }

    @SuppressLint("Range")
    public List<StudentLoginResponse> getAllSiblingData()
    {
        List<StudentLoginResponse> list = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + MY_SIBLING_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                StudentLoginResponse data = new StudentLoginResponse();
                data.setSessionId(cursor.getString(cursor.getColumnIndex(SIBLING_SESSION_ID)));
                data.setStudentId(cursor.getString(cursor.getColumnIndex(SIBLING_STUDENT_ID)));
                data.setStudentName(cursor.getString(cursor.getColumnIndex(SIBLING_STUDENT_NAME)));
                data.setClassId(cursor.getString(cursor.getColumnIndex(SIBLING_CLASS)));
                data.setSectionId(cursor.getString(cursor.getColumnIndex(SIBLING_SECTION)));
                data.setToken(cursor.getString(cursor.getColumnIndex(SIBLING_TOKEN)));
                data.setAppToken(cursor.getString(cursor.getColumnIndex(SIBLING_APP_TOKEN)));
                data.setPhoto(cursor.getString(cursor.getColumnIndex(SIBLING_PHOTO)));
                data.setLandingPage(cursor.getString(cursor.getColumnIndex(SIBLING_LANDING_PAGE)));
                list.add(data);
            } while (cursor.moveToNext());
        }
        db.close();
        // return sibling data
        return list;
    }

    public long setSessionData(SessionResponse sessionResponse) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SESSION_ID, sessionResponse.getSessionId());
        values.put(SESSION_NAME, sessionResponse.getSessionName());
        // insert row
        long id = db.insert(SESSION_DATA, null, values);
        db.close();
        return id;
    }

    @SuppressLint("Range")
    public ArrayList<SessionResponse> getSessionData()
    {
        ArrayList<SessionResponse> sessionData = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + SESSION_DATA;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SessionResponse data = new SessionResponse();
                data.setSessionName(cursor.getString(cursor.getColumnIndex(SESSION_NAME)));
                data.setSessionId(cursor.getString(cursor.getColumnIndex(SESSION_ID)));
                sessionData.add(data);
            } while (cursor.moveToNext());
        }
        db.close();
        // return notes list
        return sessionData;
    }


    public void deleteMenuItem()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ MENU_ITEM);
    }

    public void deleteSessionData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ SESSION_DATA);
    }

    public void deleteSiblingData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ MY_SIBLING_TABLE);
    }

}
