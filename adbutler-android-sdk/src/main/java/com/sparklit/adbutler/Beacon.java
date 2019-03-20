package com.sparklit.adbutler;

import com.google.gson.annotations.SerializedName;

/**
 * Models a beacon from a Placement.
 */
public class Beacon {
    @SerializedName("type")
    private String type;
    @SerializedName("url")
    private String url;

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }
}
