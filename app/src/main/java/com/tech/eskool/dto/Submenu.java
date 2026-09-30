package com.tech.eskool.dto;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Submenu {

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
}
