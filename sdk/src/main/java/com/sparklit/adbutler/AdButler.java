package com.sparklit.adbutler;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by ryuichis on 11/12/16.
 */

public class AdButler {
    static final String ADBUTLER_ENDPOINT = "https://servedbyadbutler.com/adserve/";

    private APIService _service;

    public void requestPlacement(PlacementRequestConfig config) {
        Call<ResponseBody> call = _getAPIService().requestPlacement(_buildConfigParam(config));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(response.code());

                try {
                    System.out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // :)
            }
        });
    }

    private APIService _getAPIService() {
        if (_service == null) {
            Retrofit.Builder builder = new Retrofit.Builder().baseUrl(ADBUTLER_ENDPOINT);
            _service = builder.build().create(APIService.class);
        }

        return _service;
    }

    private String _buildConfigParam(PlacementRequestConfig config) {
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
