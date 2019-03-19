package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.di.DaggerAppComponent
import com.byagowi.persiancalendar.utils.Utils

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class MainApplication : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()
        ReleaseDebugDifference.mainApplication(this)
        Utils.initUtils(applicationContext)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = DaggerAppComponent.builder().create(this)
}
