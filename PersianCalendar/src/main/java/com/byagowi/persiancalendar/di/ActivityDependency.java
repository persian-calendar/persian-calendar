package com.byagowi.persiancalendar.di;

import android.widget.Toast;

import com.byagowi.persiancalendar.MainApplication;

import javax.inject.Inject;

@PerActivity
public final class ActivityDependency {
    @Inject
    public ActivityDependency(MainApplication app) {
        Toast.makeText(app, "ActivityDependency", Toast.LENGTH_SHORT).show();
    }
}
