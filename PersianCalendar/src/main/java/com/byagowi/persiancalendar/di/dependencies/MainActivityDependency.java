package com.byagowi.persiancalendar.di.dependencies;

import com.byagowi.persiancalendar.di.scopes.PerActivity;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import javax.inject.Inject;

@PerActivity
public final class MainActivityDependency {
    @Inject
    public MainActivityDependency() {
    }

    @Inject
    MainActivity activity;

    public MainActivity getMainActivity() {
        return activity;
    }
}
