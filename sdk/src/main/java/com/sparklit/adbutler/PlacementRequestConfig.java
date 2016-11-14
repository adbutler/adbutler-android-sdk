package com.sparklit.adbutler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ryuichis on 11/13/16.
 */

public class PlacementRequestConfig {
    private int _accountId;
    private int _zoneId;
    private int _width;
    private int _height;
    private Set<String> _keywords;
    private String _click;

    public int getAccountId() {
        return _accountId;
    }

    public int getZoneId() {
        return _zoneId;
    }

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    public Set<String> getKeywords() {
        return _keywords;
    }

    public String getClick() {
        return _click;
    }

    public static class Builder {
        private int _accountId;
        private int _zoneId;
        private int _width;
        private int _height;
        private Set<String> _keywords;
        private String _click;

        public Builder(int accountId, int zoneId, int width, int height) {
            _accountId = accountId;
            _zoneId = zoneId;
            _width = width;
            _height = height;
        }

        public Builder setKeywords(Set<String> keywords) {
            _keywords = keywords;
            return this;
        }

        public Builder addKeyword(String keyword) {
            if (_keywords == null) {
                _keywords = new HashSet<>();
            }
            _keywords.add(keyword);
            return this;
        }

        public Builder setClick(String click) {
            _click = click;
            return this;
        }

        public PlacementRequestConfig build() {
            return new PlacementRequestConfig(this);
        }
    }

    private PlacementRequestConfig(Builder builder) {
        _accountId = builder._accountId;
        _zoneId = builder._zoneId;
        _width = builder._width;
        _height = builder._height;
        _keywords = builder._keywords;
        _click = builder._click;
    }
}
