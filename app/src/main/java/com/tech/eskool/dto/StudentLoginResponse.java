package com.tech.eskool.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class StudentLoginResponse implements Serializable {

    @SerializedName("session_id")
    @Expose
    private String sessionId;
    @SerializedName("student_id")
    @Expose
    private String studentId;
    @SerializedName("student_name")
    @Expose
    private String studentName;
    @SerializedName("class_id")
    @Expose
    private String classId;
    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("app_token")
    @Expose
    private String appToken;
    @SerializedName("photo")
    @Expose
    private String photo;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("college_name")
    @Expose
    private String college_name;
    @SerializedName("payment_gateway")
    @Expose
    private String payment_gateway;
    @SerializedName("enrollment_no")
    @Expose
    private String enrollment_no;
    @SerializedName("landing_page")
    @Expose
    private String landingPage;
    @SerializedName("change_password_page")
    @Expose
    private String changePasswordPage;
    @SerializedName("notification_page")
    @Expose
    private String notificationPage;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCollege_name() {
        return college_name;
    }

    public void setCollege_name(String college_name) {
        this.college_name = college_name;
    }

    public String getPayment_gateway() {
        return payment_gateway;
    }

    public void setPayment_gateway(String payment_gateway) {
        this.payment_gateway = payment_gateway;
    }

    public String getEnrollment_no() {
        return enrollment_no;
    }

    public void setEnrollment_no(String enrollment_no) {
        this.enrollment_no = enrollment_no;
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
