package com.byagowi.persiancalendar.di.dependencies;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.byagowi.persiancalendar.MainApplication;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

@Singleton
public final class AppDependency {
    private final SharedPreferences sharedPreferences;
    private final LocalBroadcastManager localBroadcastManager;

    @Inject
    public AppDependency(MainApplication app) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
        localBroadcastManager = LocalBroadcastManager.getInstance(app);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public LocalBroadcastManager getLocalBroadcastManager() {
        return localBroadcastManager;
    }
}
