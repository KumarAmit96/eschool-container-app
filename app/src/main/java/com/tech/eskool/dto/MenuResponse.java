package com.tech.eskool.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class MenuResponse {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("icon_small")
    @Expose
    private String iconSmall;
    @SerializedName("icon_large")
    @Expose
    private String iconLarge;
    @SerializedName("submenu")
    @Expose
    private List<MenuResponse> submenu;
    private int id;
    private int parentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconSmall() {
        return iconSmall;
    }

    public void setIconSmall(String iconSmall) {
        this.iconSmall = iconSmall;
    }

    public String getIconLarge() {
        return iconLarge;
    }

    public void setIconLarge(String iconLarge) {
        this.iconLarge = iconLarge;
    }

    public List<MenuResponse> getSubmenu() {
        return submenu;
    }

    public void setSubmenu(List<MenuResponse> submenu) {
        this.submenu = submenu;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}
