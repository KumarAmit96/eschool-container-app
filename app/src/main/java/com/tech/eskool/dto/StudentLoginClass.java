package com.tech.eskool.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StudentLoginClass {

    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("Response")
    @Expose
    private StudentLoginResponse response;
    @SerializedName("Message")
    @Expose
    private String message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public StudentLoginResponse getResponse() {
        return response;
    }

    public void setResponse(StudentLoginResponse response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
