package com.sparklit.adbutler;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by ryuichis on 11/13/16.
 */

public interface APIService {
    @GET("{configParam}")
    Call<PlacementResponse> requestPlacement(@Path("configParam") String config);
}
