package com.sparklit.adbutler;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class BannerView extends Fragment {
    public Banner banner;
    public boolean initializing = false;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(banner != null){
            banner.setContext(getActivity());
            return banner.getWebView();
        }
        return inflater.inflate(R.layout.banner_layout, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle bundle){
        super.onViewStateRestored(bundle);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(banner != null && banner.getMRAIDHandler() != null){
            banner.addToRoot();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Permissions.CALENDAR: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banner.getMRAIDHandler().writeCalendarEvent();
                } else {
                    // TODO show permission denied?
                }
                return;
            }
            case Permissions.PHOTO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banner.getMRAIDHandler().savePhoto();
                } else {
                    // TODO show permission denied?
                }
                return;
            }
            case Permissions.CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banner.getMRAIDHandler().makePhoneCall();
                } else {
                    // TODO show permission denied?
                }
                return;
            }
            case Permissions.SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banner.getMRAIDHandler().sendSMS();
                } else {
                    // TODO show permission denied?
                }
                return;
            }
        }
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        if(banner != null)
            banner.removeFromParent();
        super.onDetach();
    }

    public BannerView(){
    }

//    /**
//     * Create a banner in the provided view.  Use if you do not need to set any details such as custom extras, user location, coppa etc.
//     *
//     * @param accountID
//     * @param zoneID
//     * @param view
//     * @param context
//     * @param listener
//     */
//    public void initialize(int accountID, int zoneID, View view, Context context, AdListener listener){
//        initialize(new AdRequest(accountID, zoneID), view, context, listener);
//    }

    /**
     * Create a banner at the specified screen position constant.  Use if you do not need to set any details such as custom extras, user location, coppa etc.
     *
     * @param accountID
     * @param zoneID
     * @param position
     * @param context
     * @param listener
     */
    public void initialize(int accountID, int zoneID, String position, Context context, AdListener listener){
        initialize(new AdRequest(accountID, zoneID), position, context, listener);
    }

//    /**
//     * Create a banner in the provided view.  Use if you do not need to set any details such as custom extras, user location, coppa etc.
//     *
//     * @param request
//     * @param view
//     * @param context
//     * @param listener
//     */
//    public void initialize(AdRequest request, View view, Context context, AdListener listener){
//        if(initializing){
//            return;
//        }
//        initializing = true;
//        if(banner != null){
//            banner.destroy();
//        }
//        banner = new Banner(this);
//        banner.initialize(request, view, context, listener, this);
//    }

    /**
     * Create a banner in the specified position constant.  Use if you want to specify any custom data in the ad request E.G. coppa
     *
     * @param request
     * @param position
     * @param context
     * @param listener
     */
    public void initialize(AdRequest request, String position, Context context, AdListener listener){
        if(initializing){
            return;
        }
        initializing = true;
        if(banner != null){
            banner.destroy();
        }
        banner = new Banner(this);
        banner.initialize(request, position, context, listener, this);
    }
}
