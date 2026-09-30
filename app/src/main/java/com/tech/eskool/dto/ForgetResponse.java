package com.tech.eskool.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ForgetResponse {

    @SerializedName("Status")
    @Expose
    private Boolean status;

    @SerializedName("Response")
    @Expose
    private String response;

    @SerializedName("Message")
    @Expose
    private String Message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
