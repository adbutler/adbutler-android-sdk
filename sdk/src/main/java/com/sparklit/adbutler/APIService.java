package com.sparklit.adbutler;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * Created by ryuichis on 11/13/16.
 */

public interface APIService {
    @GET("{configParam}")
    Call<PlacementResponse> requestPlacement(@Path("configParam") String config);

    @GET
    Call<ResponseBody> requestPixel(@Url String url);

    @GET
    Call<ResponseBody> requestImage(@Url String imageUrl);
}
