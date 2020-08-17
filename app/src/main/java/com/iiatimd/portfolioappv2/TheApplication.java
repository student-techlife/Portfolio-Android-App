package com.iiatimd.portfolioappv2;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class TheApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }

}
