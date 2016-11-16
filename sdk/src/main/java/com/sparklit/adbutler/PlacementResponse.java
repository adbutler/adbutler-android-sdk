package com.sparklit.adbutler;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by ryuichis on 11/14/16.
 */

public class PlacementResponse {
    private String status;
    private Map<String, Placement> placements;

    public Map<String, Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(Map<String, Placement> placements) {
        this.placements = placements;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
