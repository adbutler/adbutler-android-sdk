package com.sparklit.adbutler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ryuichis on 11/14/16.
 */

public class PlacementResponse {
    private String status;
    private Map<String, Placement> placements;

    public List<Placement> getPlacements() {
        List<Placement> placements = new ArrayList<>();
        for (Map.Entry<String, Placement> placementEntry: this.placements.entrySet()) {
            placements.add(placementEntry.getValue());
        }
        return placements;
    }

    public void setPlacements(List<Placement> placements) {
        this.placements = new HashMap<>();
        for (int i = 0; i < placements.size(); i++) {
            String name = "placement_" + (i + 1);
            this.placements.put(name, placements.get(i));
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
