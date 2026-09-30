package com.tech.eskool.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtpResponse {
    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("OTP")
    @Expose
    private String oTP;
    @SerializedName("Message")
    @Expose
    private String Message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getOTP() {
        return oTP;
    }

    public void setOTP(String oTP) {
        this.oTP = oTP;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

}
