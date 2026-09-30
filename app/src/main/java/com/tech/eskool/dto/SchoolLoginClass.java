package com.tech.eskool.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SchoolLoginClass {

    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("Response")
    @Expose
    private UserLoginResponse response;
    @SerializedName("Message")
    @Expose
    private String message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public UserLoginResponse getResponse() {
        return response;
    }

    public void setResponse(UserLoginResponse response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
