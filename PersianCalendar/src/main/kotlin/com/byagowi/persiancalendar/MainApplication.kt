package com.byagowi.persiancalendar

import android.app.Application
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.utils.update

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initGlobal(this) // mostly used for things should be provided in locale level
        update(this, true)
    }
}
