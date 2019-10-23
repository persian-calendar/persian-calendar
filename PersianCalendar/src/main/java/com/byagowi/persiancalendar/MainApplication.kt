package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.di.DaggerAppComponent
import com.byagowi.persiancalendar.utils.initUtils

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class MainApplication : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()
        ReleaseDebugDifference.mainApplication(this)
        initUtils(applicationContext)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = DaggerAppComponent.factory().create(this)
}
