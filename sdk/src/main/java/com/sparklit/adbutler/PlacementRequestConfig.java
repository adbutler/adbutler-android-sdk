package com.sparklit.adbutler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ryuichis on 11/13/16.
 */

public class PlacementRequestConfig {
    private int accountId;
    private int zoneId;
    private int width;
    private int height;
    private Set<String> keywords;
    private String click;

    public int getAccountId() {
        return accountId;
    }

    public int getZoneId() {
        return zoneId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public String getClick() {
        return click;
    }

    public static class Builder {
        private int accountId;
        private int zoneId;
        private int width;
        private int height;
        private Set<String> keywords;
        private String click;

        public Builder(int accountId, int zoneId, int width, int height) {
            this.accountId = accountId;
            this.zoneId = zoneId;
            this.width = width;
            this.height = height;
        }

        public Builder setKeywords(Set<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public Builder addKeyword(String keyword) {
            if (keywords == null) {
                keywords = new HashSet<>();
            }
            keywords.add(keyword);
            return this;
        }

        public Builder setClick(String click) {
            this.click = click;
            return this;
        }

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
