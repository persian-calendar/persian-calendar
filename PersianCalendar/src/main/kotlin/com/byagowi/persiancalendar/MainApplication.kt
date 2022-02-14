package com.byagowi.persiancalendar

import android.app.Application
import com.byagowi.persiancalendar.global.initGlobal

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initGlobal(applicationContext) // mostly used for things should be provided in locale level
    }
}
