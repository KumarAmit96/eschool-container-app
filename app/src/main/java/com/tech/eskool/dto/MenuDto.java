package com.tech.eskool.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MenuDto {

    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("Response")
    @Expose
    private List<MenuResponse> menuResponse;
    @SerializedName("Message")
    @Expose
    private String message;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<MenuResponse> getMenuResponse() {
        return menuResponse;
    }

    public void setMenuResponse(List<MenuResponse> menuResponse) {
        this.menuResponse = menuResponse;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
