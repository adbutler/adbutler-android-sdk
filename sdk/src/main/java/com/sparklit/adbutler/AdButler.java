package com.sparklit.adbutler;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Makes requests against the AdButler API.
 */
public class AdButler {
    static String ADBUTLER_ENDPOINT = "https://servedbyadbutler.com/adserve/";

    private APIService service;

    /**
     * Requests a pixel.
     *
     * @param url the URL string for this pixel.
     */
    public void requestPixel(String url) {
        Call<ResponseBody> call = getAPIService().requestPixel(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(response.isSuccessful());
                // :)
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                // :)
            }
        });
    }

    /**
     * Requests a single placement.
     *
     * @param config   the configuration used for requesting one placement.
     * @param listener the results, when ready, will be given to this given listener.
     */
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

    /**
     * Requests multiple placements.
     *
     * @param configs  the configurations, each used for one placement respectively.
     * @param listener the results, when ready, will be given to this given listener.
     */
    public void requestPlacements(List<PlacementRequestConfig> configs, final PlacementResponseListener listener) {
        final List<Call<PlacementResponse>> calls = new ArrayList<>();
        for (PlacementRequestConfig config : configs) {
            calls.add(getAPIService().requestPlacement(buildConfigParam(config)));
        }
        final List<PlacementResponse> responses = new ArrayList<>();
        final List<Throwable> throwables = new ArrayList<>();
        for (Call<PlacementResponse> call : calls) {
            call.enqueue(new Callback<PlacementResponse>() {
                @Override
                public void onResponse(Call<PlacementResponse> call, Response<PlacementResponse> response) {
                    responses.add(response.body());
                    checkResults(listener, calls, responses, throwables);
                }

                @Override
                public void onFailure(Call<PlacementResponse> call, Throwable t) {
                    throwables.add(t);
                    checkResults(listener, calls, responses, throwables);
                }
            });
        }
    }

    private void checkResults(final PlacementResponseListener listener,
                                    List<Call<PlacementResponse>> calls,
                                    List<PlacementResponse> responses,
                                    List<Throwable> throwables) {
        if (responses.size() + throwables.size() != calls.size()) {
            return;
        }

        if (!throwables.isEmpty()) {
            listener.error(throwables.get(0));
            return;
        }

        List<Placement> placements = new ArrayList<>();
        for (PlacementResponse response : responses) {
            if (response.getStatus().equals("SUCCESS")) {
                placements.addAll(response.getPlacements());
            }
        }
        String status = placements.isEmpty() ? "NO_ADS" : "SUCCESS";
        PlacementResponse placementResponse = new PlacementResponse();
        placementResponse.setStatus(status);
        placementResponse.setPlacements(placements);

        listener.success(placementResponse);
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
