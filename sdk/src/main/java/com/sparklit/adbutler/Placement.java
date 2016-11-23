package com.sparklit.adbutler;

/**
 * Created by ryuichis on 11/14/16.
 */

public class Placement {
    private int bannerId;
    private String redirectUrl;
    private String imageUrl;
    private int width;
    private int height;
    private String altText;
    private String target;
    private String trackingPixel;
    private String accupixelUrl;
    private String refreshUrl;
    private String refreshTime;
    private String body;

    public int getBannerId() {
        return bannerId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getAltText() {
        return altText;
    }

    public String getTarget() {
        return target;
    }

    public String getTrackingPixel() {
        return trackingPixel;
    }

    public String getAccupixelUrl() {
        return accupixelUrl;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    public String getRefreshTime() {
        return refreshTime;
    }

    public String getBody() {
        return body;
    }

    public void recordImpression() {
        AdButler adButler = new AdButler();
        if (getAccupixelUrl() != null) {
            adButler.requestPixel(getAccupixelUrl());
        } else if (getTrackingPixel() != null) {
            adButler.requestPixel(getTrackingPixel());
        }
    }

    public void recordClick() {
        if (getRedirectUrl() != null) {
            AdButler adButler = new AdButler();
            adButler.requestPixel(getRedirectUrl());
        }
    }
}

