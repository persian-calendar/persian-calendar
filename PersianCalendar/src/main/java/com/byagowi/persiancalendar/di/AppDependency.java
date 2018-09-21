package com.byagowi.persiancalendar.di;

import android.widget.Toast;

import com.byagowi.persiancalendar.MainApplication;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AppDependency {
    @Inject
    public AppDependency(MainApplication app) {
        Toast.makeText(app, "AppDependency", Toast.LENGTH_SHORT).show();
    }
}
