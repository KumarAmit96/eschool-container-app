package com.tech.eskool.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class StudentSiblingRecord {

    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("Response")
    @Expose
    private ArrayList<StudentLoginResponse> response;
    @SerializedName("Message")
    @Expose
    private String message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<StudentLoginResponse> getResponse() {
        return response;
    }

    public void setResponse(ArrayList<StudentLoginResponse> response) {
        this.response = response;
    }
}
