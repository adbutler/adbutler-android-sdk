package com.sparklit.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.sparklit.adbutler.AdButler;
import com.sparklit.adbutler.Placement;
import com.sparklit.adbutler.PlacementRequestConfig;
import com.sparklit.adbutler.PlacementResponse;
import com.sparklit.adbutler.PlacementResponseListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void requestPlacement(View view) {
        PlacementRequestConfig config = new PlacementRequestConfig.Builder(153105, 214764, 300, 250).build();
        AdButler adbutler = new AdButler();
        adbutler.requestPlacement(config, new PlacementResponseListener() {
            @Override
            public void success(PlacementResponse response) {
                System.out.println(response.getStatus());
                for (Placement placement : response.getPlacements()) {
                    System.out.println(placement.getBannerId());
                }
            }

            @Override
            public void error(Throwable throwable) {

            }
        });
    }

    public void requestPlacements(View view) {
        PlacementRequestConfig config1 = new PlacementRequestConfig.Builder(153105, 214764, 300, 250).build();
        String[] keywords = {"sample2"};
        Set<String> keywordSet = new HashSet<>();
        Collections.addAll(keywordSet, keywords);

        PlacementRequestConfig config2 = new PlacementRequestConfig.Builder(153105, 214764, 300, 250)
                .setKeywords(keywordSet)
                .build();

        List<PlacementRequestConfig> configs = new ArrayList<>();
        configs.add(config1);
        configs.add(config2);

        AdButler adbutler = new AdButler();
        adbutler.requestPlacements(configs, new PlacementResponseListener() {
            @Override
            public void success(PlacementResponse response) {
                System.out.println(response.getStatus());
                for (Placement placement : response.getPlacements()) {
                    System.out.println(placement.getBannerId());
                }
            }

            @Override
            public void error(Throwable throwable) {

            }
        });
    }
}
