package com.byagowi.persiancalendar

import android.app.Application
import com.byagowi.persiancalendar.utils.initUtils

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReleaseDebugDifference.mainApplication(this)
        initUtils(applicationContext)
    }
}
