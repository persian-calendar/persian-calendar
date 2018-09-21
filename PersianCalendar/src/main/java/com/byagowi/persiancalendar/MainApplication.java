package com.byagowi.persiancalendar;

import com.byagowi.persiancalendar.di.DaggerAppComponent;
import com.byagowi.persiancalendar.util.Utils;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

public class MainApplication extends DaggerApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        ReleaseDebugDifference.mainApplication(this);
        Utils.initUtils(getApplicationContext());
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().create(this);
    }
}
