package com.tech.eskool.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserLoginResponse implements Serializable {


    @SerializedName("UserId")
    @Expose
    private String userId;
    @SerializedName("UserName")
    @Expose
    private String userName;
    @SerializedName("EmpId")
    @Expose
    private String empId;
    @SerializedName("EmpType")
    @Expose
    private String empType;
    @SerializedName("class_id")
    @Expose
    private String classId;
    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("Token")
    @Expose
    private String token;
    @SerializedName("app_token")
    @Expose
    private String appToken;
    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("photo")
    @Expose
    private String photo;
    @SerializedName("college_name")
    @Expose
    private String college_name;
    @SerializedName("landing_page")
    @Expose
    private String landingPage;
    @SerializedName("change_password_page")
    @Expose
    private String changePasswordPage;
    @SerializedName("notification_page")
    @Expose
    private String notificationPage;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmpType() {
        return empType;
    }

    public void setEmpType(String empType) {
        this.empType = empType;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCollege_name() {
        return college_name;
    }

    public void setCollege_name(String college_name) {
        this.college_name = college_name;
    }

    public String getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(String landingPage) {
        this.landingPage = landingPage;
    }

    public String getChangePasswordPage() {
        return changePasswordPage;
    }

    public void setChangePasswordPage(String changePasswordPage) {
        this.changePasswordPage = changePasswordPage;
    }

    public String getNotificationPage() {
        return notificationPage;
    }

    public void setNotificationPage(String notificationPage) {
        this.notificationPage = notificationPage;
    }
}
