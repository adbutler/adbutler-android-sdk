package com.sparklit.adbutler;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

interface APIService {
    @GET("{configParam}")
    Call<PlacementResponse> requestPlacement(@Path("configParam") String config);

    @GET
    Call<ResponseBody> requestPixel(@Url String url);
}
