package com.byagowi.persiancalendar

import android.app.Application
import androidx.compose.runtime.Composer
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.utils.update

class MainApplication : Application() {
    @OptIn(ExperimentalComposeRuntimeApi::class)
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEVELOPMENT) Composer.setDiagnosticStackTraceEnabled(BuildConfig.DEBUG)
        initGlobal(applicationContext) // mostly used for things should be provided in locale level
        update(this, true)
    }
}
