package com.byagowi.persiancalendar.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.update
import com.byagowi.persiancalendar.variants.debugLog

/**
 * The Calendar Service that updates widget time and clock and build/update
 * calendar notification.
 */
class ApplicationService : Service() {
    private val receiver = BroadcastReceivers()

    override fun onBind(paramIntent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        debugLog("${ApplicationService::class.java.name} start")

        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            // addAction(Intent.ACTION_TIME_TICK)
        }
        registerReceiver(receiver, intentFilter)

        updateStoredPreference(applicationContext)
        update(applicationContext, updateDate = true)

        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }.onFailure(logException)
        super.onDestroy()
    }
}
