package com.tech.eskool.rest_service;

import com.tech.eskool.dto.ForgetResponse;
import com.tech.eskool.dto.MenuDto;
import com.tech.eskool.dto.OtpResponse;
import com.tech.eskool.dto.SchoolLoginClass;
import com.tech.eskool.dto.SessionClass;
import com.tech.eskool.dto.StudentLoginClass;
import com.tech.eskool.dto.StudentSiblingRecord;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Api_Interface {

    //Admin and Teacher Login
    @FormUrlEncoded
    @POST("user/Api-Login.php")
    Call<SchoolLoginClass> getSchoolLogin(@Field("username") String username, @Field("pass") String pass,
                                          @Field("college_code") String college_code, @Field("device_token") String fcmToken,
                                          @Field("device_type") String deviceType);

    //Student Login
    @FormUrlEncoded
    @POST("student/Api-Login.php")
    Call<StudentLoginClass> getStudentLogin(@Field("username") String username, @Field("pass") String pass,
                                            @Field("college_code") String college_code, @Field("device_token") String fcmToken,
                                            @Field("device_type") String deviceType);

    //Student manually reset password
    @FormUrlEncoded
    @POST("student/Api-Change-Password.php")
    Call<ForgetResponse> getResetPasswordDetail(@Field("token") String token, @Field("session_id") String session_id,
                                                @Field("current_password") String current_password, @Field("new_password") String new_password,
                                                @Field("college_code") String college_code);

    @FormUrlEncoded
    @POST("student/Api-Otp-Request.php")
    Call<OtpResponse> getForgetPasswordDetail(@Field("enrollment_no") String enrollment_no, @Field("college_code") String college_code);

    //Student password otp call
    @FormUrlEncoded
    @POST("student/Api-Otp-Change-Password.php")
    Call<ForgetResponse> getOTPPasswordDetail(@Field("otp") String otp, @Field("password") String password, @Field("college_code") String college_code);

    @GET("student/Api-Student-Menu.php")
    Call<MenuDto> getMenuForStudent(@Query("token") String token, @Query("college_code") String college_code);

    @GET("user/Api-User-Menu.php")
    Call<MenuDto> getMenuForUser(@Query("token") String token, @Query("college_code") String college_code);

    @GET("student/Api-Sibling.php")
    Call<StudentSiblingRecord> getSiblingData(@Query("token") String token, @Query("session_id") String session_id, @Query("college_code") String college_code);

    @GET("Api-Details.php?action=session_details")
    Call<SessionClass> getSessionData(@Query("college_code") String college_code);
}
