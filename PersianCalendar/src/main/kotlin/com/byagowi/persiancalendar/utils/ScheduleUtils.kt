package com.byagowi.persiancalendar.utils

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.work.*
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.service.UpdateWorker
import java.util.*
import java.util.concurrent.TimeUnit

val enableWorkManager: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun scheduleAlarmManagerUpdates(context: Context) = runCatching {
    if (enableWorkManager) return@runCatching
    val alarmManager = context.getSystemService<AlarmManager>() ?: return@runCatching

    val startTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 1)
        add(Calendar.DATE, 1)
    }

    val dailyPendingIntent = PendingIntent.getBroadcast(
        context, LOAD_APP_ID,
        Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_RESTART_APP),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    alarmManager.set(AlarmManager.RTC, startTime.timeInMillis, dailyPendingIntent)

    // There are simpler triggers on older Androids like SCREEN_ON but they
    // are not available anymore, lets register an hourly alarm for >= Oreo
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val threeHoursPendingIntent = PendingIntent.getBroadcast(
            context, THREE_HOURS_APP_ID,
            Intent(context, BroadcastReceivers::class.java).setAction(BROADCAST_UPDATE_APP),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            // Start from one hour from now
            System.currentTimeMillis() + ONE_HOUR_IN_MILLIS,
            THREE_HOURS_IN_MILLIS, threeHoursPendingIntent
        )
    }
}.onFailure(logException).let {}

fun startEitherServiceOrWorker(context: Context) {
    if (enableWorkManager) {
        runCatching {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATE_TAG, ExistingPeriodicWorkPolicy.REPLACE,
                // An hourly task to call UpdateWorker.doWork
                PeriodicWorkRequest.Builder(UpdateWorker::class.java, 1L, TimeUnit.HOURS).build()
            )
        }.onFailure(logException).getOrNull().debugAssertNotNull
    } else {
        val isRunning = context.getSystemService<ActivityManager>()?.let { am ->
            runCatching {
                am.getRunningServices(Integer.MAX_VALUE).any {
                    ApplicationService::class.java.name == it.service.className
                }
            }.onFailure(logException).getOrNull()
        } ?: false

        if (!isRunning) runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ContextCompat.startForegroundService(
                    context, Intent(context, ApplicationService::class.java)
                )
            context.startService(Intent(context, ApplicationService::class.java))
        }.onFailure(logException)
    }
}
