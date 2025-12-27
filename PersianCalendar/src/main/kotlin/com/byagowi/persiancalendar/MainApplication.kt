package com.byagowi.persiancalendar

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.utils.update

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initGlobal(this) // mostly used for things should be provided in locale level
        update(this, true)

        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            // addAction(Intent.ACTION_TIME_TICK)
        }
        registerReceiver(receiver, intentFilter)
    }

    private val receiver = BroadcastReceivers()
}
