package com.tech.eskool.dto;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SessionResponse {

    @SerializedName("session_id")
    @Expose
    private String sessionId;
    @SerializedName("session_name")
    @Expose
    private String sessionName;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

}
