package com.byagowi.persiancalendar

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        enqueueUpdateWorker()
    }

    private fun enqueueUpdateWorker() {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PeriodicUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<UpdateWorker>(1.hours.toJavaDuration())
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()
        )
    }
}

class BroadcastReceivers : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Nothing right now, handled by MainApplication
            let {}
        }
    }
}
