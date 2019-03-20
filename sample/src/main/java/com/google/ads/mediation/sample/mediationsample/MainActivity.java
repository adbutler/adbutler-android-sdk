/*
 * Copyright (C) 2019 Sparklit Networks Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ads.mediation.sample.mediationsample;

import android.app.FragmentManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sparklit.adbutler.AdRequest;
import com.sparklit.adbutler.BannerView;
import com.sparklit.adbutler.Positions;
import com.sparklit.adbutler.Interstitial;
import com.sparklit.adbutler.AdListener;
import com.sparklit.adbutler.ErrorCode;
import com.sparklit.adbutler.AdButler;

import java.util.Date;
import java.util.Random;

import android.view.View;

/**
 * A simple {@link android.app.Activity} that displays adds using the sample adapter and sample
 * custom event.
 */
public class MainActivity extends AppCompatActivity {

    private BannerView bannerView;
    private Interstitial interstitial;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AdButler.initialize(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    public void onGetInterstitialClick(View view){
        interstitial = new Interstitial();
        AdRequest request = new AdRequest(172084, 349510);
        request.setCoppa(0);
        request.setAge(30);
        request.setGender(getUserGender());
        request.setLocation(getUserLocation());
        request.setBirthday(new Date());
        interstitial.initialize(request, this, new AdListener() {
            @Override
            public void onAdFetchSucceeded() {
                super.onAdFetchSucceeded();
            }

            @Override
            public void onInterstitialReady(){
                super.onInterstitialReady();
                if(interstitial.isReady){
                    interstitial.show();
                }
            }

            @Override
            public void onAdFetchFailed(ErrorCode code) {
                super.onAdFetchFailed(code);
            }

            @Override
            public void onInterstitialDisplayed() {
                super.onInterstitialDisplayed();
            }

            @Override
            public void onAdExpanded(){
                super.onAdExpanded();
            }

            @Override
            public void onAdResized(){
                super.onAdResized();
            }

            @Override
            public void onAdLeavingApplication(){
                super.onAdLeavingApplication();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }
        });
    }


    public void onGetBannerClick(View view){
        FragmentManager fm = getFragmentManager();
        bannerView = (BannerView)fm.findFragmentById(R.id.adbutler_fragment);
        AdRequest request = new AdRequest(172084, 349510);
        request.setCoppa(0);
        request.setAge(30);
        request.setGender(getUserGender());
        request.setLocation(getUserLocation());
        request.setBirthday(new Date());
        bannerView.initialize(request, Positions.BOTTOM_CENTER, this, new AdListener() {
            @Override
            public void onAdFetchSucceeded() {
                super.onAdFetchSucceeded();
            }

            @Override
            public void onAdFetchFailed(ErrorCode code) {
                super.onAdFetchFailed(code);
            }

            @Override
            public void onInterstitialDisplayed() {
                super.onInterstitialDisplayed();
            }

            @Override
            public void onAdExpanded(){
                super.onAdExpanded();
            }

            @Override
            public void onAdResized(){
                super.onAdResized();
            }

            @Override
            public void onAdLeavingApplication(){
                super.onAdLeavingApplication();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }
        });
    }

    // dummy gender
    public int getUserGender() {
        Random rand = new Random();
        int i = rand.nextInt(3);
        int[] genders = {AdRequest.GENDER_UNKNOWN, AdRequest.GENDER_MALE, AdRequest.GENDER_FEMALE};
        return genders[i];
    }

    public Location getUserLocation() {
        Location loc = new Location("Dummy");
        loc.setLatitude(37.4220);
        loc.setLongitude(122.0841);
        return loc;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        bannerView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
