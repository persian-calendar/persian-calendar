package com.byagowi.persiancalendar

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.utils.getDeviceEventsChangeFlow
import com.byagowi.persiancalendar.variants.debugLog
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initGlobal(applicationContext) // mostly used for things should be provided in locale level

        getDeviceEventsChangeFlow(applicationContext)
            .catch { debugLog("error: $it") }
            .onEach { debugLog("something in calendar changed!") }
            .launchIn(ProcessLifecycleOwner.get().lifecycle.coroutineScope)
    }
}
