package com.iiatimd.portfolioappv2.Network;

import android.content.Context;
import android.net.ConnectivityManager;

public class InternetCheck {
    private static final String TAG = "internetCheck";

    Context context;

    public InternetCheck(Context context){
        this.context = context;
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
