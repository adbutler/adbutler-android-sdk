package com.example.sdktester;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sparklit.adbutler.*;

import java.text.ParseException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Button btnGetBanner;
    private Button btnGetInterstitial;
    private Button btnGetVASTVideo;
    private Button btnDisplayInterstitial;
    private Button btnDestroy;

    private Button btnTopLeft;
    private Button btnTopCenter;
    private Button btnTopRight;
    private Button btnCenterLeft;
    private Button btnCenter;
    private Button btnCenterRight;
    private Button btnBottomLeft;
    private Button btnBottomCenter;
    private Button btnBottomRight;
    HashMap<Button, String> positions = new HashMap<Button, String>() {
        {
            put(btnTopLeft, Positions.TOP_LEFT);
            put(btnTopCenter, Positions.TOP_CENTER);
            put(btnTopRight, Positions.TOP_RIGHT);
            put(btnCenterLeft, Positions.LEFT_CENTER);
            put(btnCenter, Positions.CENTER);
            put(btnCenterRight, Positions.RIGHT_CENTER);
            put(btnBottomLeft, Positions.BOTTOM_LEFT);
            put(btnBottomCenter, Positions.BOTTOM_CENTER);
            put(btnBottomRight, Positions.BOTTOM_RIGHT);
        }
    };

    private Button selectedPosition;

    private StringBuilder logBuilder = new StringBuilder();
    private EditText txtAccountID;
    private EditText txtZoneID;
    private EditText txtPublisherID;
    private TextView txtLog;

    private int accountID;
    private int zoneID;
    private int publisherID;
    private String position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI(findViewById(R.id.containerView));

        btnGetBanner = (Button)findViewById(R.id.btnGetBanner);
        btnGetInterstitial = (Button)findViewById(R.id.btnGetInterstitial);
        btnGetVASTVideo = (Button)findViewById(R.id.btnGetVASTVideo);
        btnDisplayInterstitial = (Button)findViewById(R.id.btnDisplayInterstitial);
        btnDestroy = (Button)findViewById(R.id.btnDestroy);

        btnTopLeft = (Button)findViewById(R.id.btnTopLeft);
        btnTopCenter = (Button)findViewById(R.id.btnTopCenter);
        btnTopRight = (Button)findViewById(R.id.btnTopRight);
        btnCenterLeft = (Button)findViewById(R.id.btnCenterLeft);
        btnCenter = (Button)findViewById(R.id.btnCenter);
        btnCenterRight = (Button)findViewById(R.id.btnCenterRight);
        btnBottomLeft = (Button)findViewById(R.id.btnBottomLeft);
        btnBottomCenter = (Button)findViewById(R.id.btnBottomCenter);
        btnBottomRight = (Button) findViewById(R.id.btnBottomRight);



        txtAccountID = (EditText) findViewById(R.id.txtAccountID);
        txtZoneID = (EditText) findViewById(R.id.txtZoneID);
        txtPublisherID = (EditText) findViewById(R.id.txtPublisherID);
        txtLog = (TextView)findViewById(R.id.txtLog);

        selectedPosition = btnBottomCenter;
    }

    protected void onGetBannerClick(View v){
        validateInputs(false);
    }

    protected void onGetInterstitialClick(View v){
        log("Long ass text.  super long.  too long man.  stop typing immediately.");
    }

    protected void onGetVASTVideoClick(View v){

    }

    protected void onDisplayInterstitialClick(View v){

    }

    protected void onDestroyClick(View v){

    }

    private Boolean validateInputs(Boolean includePublisher){
        String text = txtAccountID.getText().toString();
        if(!text.isEmpty()){
            try{
                int val = Integer.parseInt(text);
                accountID = val;
            }catch(Exception ex){
                log(ex.getMessage());
                return false;
            }
        }else{
            log("No Account ID provided.");
            return false;
        }

        text = txtZoneID.getText().toString();
        if(!text.isEmpty()){
            try{
                int val = Integer.parseInt(text);
                zoneID = val;
            }catch(Exception ex){
                log(ex.getMessage());
                return false;
            }
        }else{
            log("No Zone ID provided.");
            return false;
        }

        if(includePublisher){
            text = txtPublisherID.getText().toString();
            if(!text.isEmpty()){
                try{
                    int val = Integer.parseInt(text);
                    publisherID = val;
                }catch(Exception ex){
                    log(ex.getMessage());
                    return false;
                }
            }else{
                log("No Publisher ID provided.");
                return false;
            }
        }
        return true;
    }

    protected void onPositionClick(View v){
        selectedPosition.setBackgroundResource(R.color.colorPrimaryDark);
        selectedPosition = (Button)v;
        selectedPosition.setBackgroundResource(R.color.colorPrimary);
        position = positions.get(selectedPosition);
    }

    private void log(String str){
        logBuilder.insert(0, "\n> " + str);
        if(logBuilder.length() > 32767){
            logBuilder.setLength(32764); // int16
            logBuilder.append("...");
        }
        txtLog.setText(logBuilder.toString());
    }

    // thanks https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
