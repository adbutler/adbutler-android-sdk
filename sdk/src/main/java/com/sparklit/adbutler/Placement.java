package com.sparklit.adbutler;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ryuichis on 11/14/16.
 */

public class Placement {
    @SerializedName("banner_id")
    private int _bannerId;

    @SerializedName("redirect_url")
    private String _redirectUrl;

    @SerializedName("image_url")
    private String _imageUrl;

    @SerializedName("width")
    private int _width;

    @SerializedName("height")
    private int _height;

    @SerializedName("alt_text")
    private String _altText;

    @SerializedName("target")
    private String _target;

    @SerializedName("tracking_pixel")
    private String _trackingPixel;

    @SerializedName("accupixel_url")
    private String _accupixelUrl;

    @SerializedName("refresh_url")
    private String _refreshUrl;

    @SerializedName("refresh_time")
    private String _refreshTime;

    @SerializedName("body")
    private String _body;

    public int getBannerId() {
        return _bannerId;
    }

    public void setBannerId(int bannerId) {
        _bannerId = bannerId;
    }

    public String getRedirectUrl() {
        return _redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        _redirectUrl = redirectUrl;
    }

    public String getImageUrl() {
        return _imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        _imageUrl = imageUrl;
    }

    public int getWidth() {
        return _width;
    }

    public void setWidth(int width) {
        _width = width;
    }

    public int getHeight() {
        return _height;
    }

    public void setHeight(int height) {
        _height = height;
    }

    public String getAltText() {
        return _altText;
    }

    public void setAltText(String altText) {
        _altText = altText;
    }

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }

    public String getTrackingPixel() {
        return _trackingPixel;
    }

    public void setTrackingPixel(String trackingPixel) {
        _trackingPixel = trackingPixel;
    }

    public String getAccupixelUrl() {
        return _accupixelUrl;
    }

    public void setAccupixelUrl(String accupixelUrl) {
        _accupixelUrl = accupixelUrl;
    }

    public String getRefreshUrl() {
        return _refreshUrl;
    }

    public void setRefreshUrl(String refreshUrl) {
        _refreshUrl = refreshUrl;
    }

    public String getRefreshTime() {
        return _refreshTime;
    }

    public void setRefreshTime(String refreshTime) {
        _refreshTime = refreshTime;
    }

    public String getBody() {
        return _body;
    }

    public void setBody(String body) {
        _body = body;
    }

}

