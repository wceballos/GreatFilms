package com.example.greatfilms.NetworkUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

// Using the example from:
// https://developer.android.com/training/monitoring-device-state/connectivity-status-type

public class Network {

    /**
     * Checks if network is available.
     *
     * @param context Application context.
     * @return True if network is available.
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean isConnected = false;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork;
        if(cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
        }
        return  isConnected;
    }
}
