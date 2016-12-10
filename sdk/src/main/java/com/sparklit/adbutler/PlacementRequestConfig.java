package com.sparklit.adbutler;

import java.util.HashSet;
import java.util.Set;

/**
 * Configurations for requesting a Placement.
 */
public class PlacementRequestConfig {
    private int accountId;
    private int zoneId;
    private int width;
    private int height;
    private Set<String> keywords;
    private String click;

    /**
     * The account ID for this request.
     */
    public int getAccountId() {
        return accountId;
    }
    /**
     * The publisher zone ID to select advertisements from.
     */
    public int getZoneId() {
        return zoneId;
    }
    /**
     * The width of the publisher zone.
     */
    public int getWidth() {
        return width;
    }
    /**
     * The height of the publisher zone.
     */
    public int getHeight() {
        return height;
    }
    /**
     * A comma delimited list of keywords.
     */
    public Set<String> getKeywords() {
        return keywords;
    }
    /**
     * A pass-through click URL.
     */
    public String getClick() {
        return click;
    }

    /**
     * Builder to configure the parameters used in requesting a Placement.
     */
    public static class Builder {
        private int accountId;
        private int zoneId;
        private int width;
        private int height;
        private Set<String> keywords;
        private String click;

        /**
         * @param accountId The account ID for this request.
         * @param zoneId    The publisher zone ID to select advertisements from.
         * @param width     The width of the publisher zone.
         * @param height    The height of the publisher zone.
         */
        public Builder(int accountId, int zoneId, int width, int height) {
            this.accountId = accountId;
            this.zoneId = zoneId;
            this.width = width;
            this.height = height;
        }

        /**
         * Sets keywords used in the request.
         * This will override all existing keywords.
         *
         * @param keywords Keywords used in the request
         */
        public Builder setKeywords(Set<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        /**
         * Adds one keyword for the request.
         *
         * @param keyword Keyword used in the request
         */
        public Builder addKeyword(String keyword) {
            if (keywords == null) {
                keywords = new HashSet<>();
            }
            keywords.add(keyword);
            return this;
        }

        /**
         * Sets the pass-through click used in the request.
         * This will override existing click URL.
         *
         * @param click Click URL used in the request
         */
        public Builder setClick(String click) {
            this.click = click;
            return this;
        }

        /**
         * @return The PlacementRequestConfig that can be used in requesting a Placement.
         */
        public PlacementRequestConfig build() {
            return new PlacementRequestConfig(this);
        }
    }

    private PlacementRequestConfig(Builder builder) {
        accountId = builder.accountId;
        zoneId = builder.zoneId;
        width = builder.width;
        height = builder.height;
        keywords = builder.keywords;
        click = builder.click;
    }
}
