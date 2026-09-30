package com.tech.eskool.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SessionClass {

    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("Response")
    @Expose
    private List<SessionResponse> response = null;
    @SerializedName("Message")
    @Expose
    private String Message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<SessionResponse> getResponse() {
        return response;
    }

    public void setResponse(List<SessionResponse> response) {
        this.response = response;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
