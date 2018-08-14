package com.byagowi.persiancalendar;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class ReleaseDebugDifference {
    public static void mainApplication(Application app) {
        if (LeakCanary.isInAnalyzerProcess(app)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(app);
    }
}