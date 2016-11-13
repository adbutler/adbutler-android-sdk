package com.sparklit.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.sparklit.adbutler.AdButler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void requestPlacement(View view) {
        AdButler adbutler = new AdButler();
        adbutler.requestPlacement();
    }
}
