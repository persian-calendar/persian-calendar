package com.byagowi.persiancalendar;

import android.app.Application;

import com.byagowi.persiancalendar.util.Utils;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ReleaseDebugDifference.mainApplication(this);
        Utils.initUtils(getApplicationContext());
    }
}
