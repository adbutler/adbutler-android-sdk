package com.sparklit.adbutler;

/**
 * Models the Placement with all its properties.
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

    /**
     * The unique ID of the banner returned.
     */
    public int getBannerId() {
        return bannerId;
    }
    /**
     * A pass-through click redirect URL.
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }
    /**
     * The image banner URL.
     */
    public String getImageUrl() {
        return imageUrl;
    }
    /**
     * The width of this placement.
     */
    public int getWidth() {
        return width;
    }
    /**
     * The height of this placement.
     */
    public int getHeight() {
        return height;
    }
    /**
     * Alternate text for screen readers on the web.
     */
    public String getAltText() {
        return altText;
    }
    /**
     * An HTML target attribute.
     */
    public String getTarget() {
        return target;
    }
    /**
     * An optional user-specified tracking pixel URL.
     */
    public String getTrackingPixel() {
        return trackingPixel;
    }
    /**
     * Used to record an impression for this request.
     */
    public String getAccupixelUrl() {
        return accupixelUrl;
    }
    /**
     * Contains a zone URL to request a new ad.
     */
    public String getRefreshUrl() {
        return refreshUrl;
    }
    /**
     * The user-specified delay between refresh URL requests.
     */
    public String getRefreshTime() {
        return refreshTime;
    }
    /**
     * The HTML markup of an ad request.
     */
    public String getBody() {
        return body;
    }

    /**
     * Sends request to record impression for this Placement.
     */
    public void recordImpression() {
        AdButler adButler = new AdButler();
        if (getAccupixelUrl() != null) {
            adButler.requestPixel(getAccupixelUrl());
        }
        if (getTrackingPixel() != null) {
            adButler.requestPixel(getTrackingPixel());
        }
    }

    /**
     * Sends request to record click for this Placement.
     */
    public void recordClick() {
        if (getRedirectUrl() != null) {
            AdButler adButler = new AdButler();
            adButler.requestPixel(getRedirectUrl());
        }
    }
}

