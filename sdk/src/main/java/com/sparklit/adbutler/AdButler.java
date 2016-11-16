package com.sparklit.adbutler;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ryuichis on 11/12/16.
 */

public class AdButler {
    static final String ADBUTLER_ENDPOINT = "https://servedbyadbutler.com/adserve/";

    private APIService service;

    public void requestPlacement(PlacementRequestConfig config, final PlacementResponseListener listener) {
        Call<PlacementResponse> call = getAPIService().requestPlacement(buildConfigParam(config));
        call.enqueue(new Callback<PlacementResponse>() {
            @Override
            public void onResponse(Call<PlacementResponse> call, Response<PlacementResponse> response) {
                listener.success(response.body());
            }

            @Override
            public void onFailure(Call<PlacementResponse> call, Throwable t) {
                listener.error(t);
            }
        });
    }

    private APIService getAPIService() {
        if (service == null) {
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(ADBUTLER_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson));
            service = builder.build().create(APIService.class);
        }

        return service;
    }

    private String buildConfigParam(PlacementRequestConfig config) {
        String urlString = String.format(";ID=%d;size=%dx%d;setID=%d",
                config.getAccountId(),
                config.getWidth(),
                config.getHeight(),
                config.getZoneId());
        if (config.getKeywords() != null && config.getKeywords().size() > 0) {
            String keywordsQuery = null;
            for (String keyword : config.getKeywords()) {
                if (keywordsQuery == null) {
                    keywordsQuery = ";kw=" + keyword;
                } else {
                    keywordsQuery += "," + keyword;
                }
            }
            urlString += keywordsQuery;
        }
        if (config.getClick() != null) {
            urlString += ";click=" + config.getClick();
        }
        urlString += ";type=json";
        return urlString;
    }
}
