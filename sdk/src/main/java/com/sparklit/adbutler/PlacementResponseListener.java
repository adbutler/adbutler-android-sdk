package com.sparklit.adbutler;

/**
 * Created by ryuichis on 11/14/16.
 */

public interface PlacementResponseListener {
    public void success(PlacementResponse response);
    public void error(Throwable throwable);
}
