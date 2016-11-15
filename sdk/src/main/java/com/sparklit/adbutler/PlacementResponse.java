package com.sparklit.adbutler;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by ryuichis on 11/14/16.
 */

public class PlacementResponse {
    @SerializedName("status")
    private String _status;

    @SerializedName("placements")
    private Map<String, Placement> _placements;

    public Map<String, Placement> getPlacements() {
        return _placements;
    }

    public void setPlacements(Map<String, Placement> placements) {
        _placements = placements;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        _status = status;
    }

}
