package com.sparklit.adbutler;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by ryuichis on 11/13/16.
 */

public interface APIService {
    @GET("{configParam}")
    Call<ResponseBody> requestPlacement(@Path("configParam") String config);
}
