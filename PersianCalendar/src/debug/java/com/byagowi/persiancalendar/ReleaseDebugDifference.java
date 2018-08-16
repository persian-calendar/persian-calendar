package com.byagowi.persiancalendar;

import android.app.Application;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
//import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;

public class ReleaseDebugDifference {
    public static void mainApplication(Application app) {
        // Setup Crashlytics
        Fabric.with(app, new Crashlytics());

        // Setup LeakCanary
        if (LeakCanary.isInAnalyzerProcess(app)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                .detectAll()
                .penaltyLog()
                //.penaltyDeath()
                .build());
        LeakCanary.install(app);

        // Setup AndroidDevMetrics
        // AndroidDevMetrics.initWith(app);
    }
}