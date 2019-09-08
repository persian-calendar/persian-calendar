package com.byagowi.persiancalendar.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log

import com.byagowi.persiancalendar.utils.UpdateUtils
import com.byagowi.persiancalendar.utils.Utils

import java.lang.ref.WeakReference

/**
 * The Calendar Service that updates widget time and clock and build/update
 * calendar notification.
 *
 * @author Ebrahim Byagowi <ebrahim></ebrahim>@byagowi.com>
 */
class ApplicationService : Service() {
    private val receiver = BroadcastReceivers()

    override fun onBind(paramIntent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        instance = WeakReference(this)
        Log.d(ApplicationService::class.java.name, "start")

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED)
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)
        //        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, intentFilter)

        Utils.updateStoredPreference(applicationContext)
        Utils.loadApp(this)
        UpdateUtils.update(applicationContext, true)

        return Service.START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(receiver)
        } catch (ignore: Exception) {
            // Really can't do much here
        }

        super.onDestroy()
    }

    companion object {

        private var instance: WeakReference<ApplicationService>? = null

        fun getInstance(): ApplicationService? {
            return if (instance == null) null else instance!!.get()
        }
    }
}
