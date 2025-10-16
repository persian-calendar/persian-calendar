package com.byagowi.persiancalendar.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.byagowi.persiancalendar.BROADCAST_UPDATE_APP
import com.byagowi.persiancalendar.THREE_HOURS_INEXACT_ALARM_CODE
import com.byagowi.persiancalendar.UPDATE_TAG
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.service.UpdateWorker
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun startWorker(context: Context) {
    scheduleWorkManager(context)
    scheduleThreeHoursAlarmManager(context)
}

private fun scheduleWorkManager(context: Context) {
    runCatching {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UPDATE_TAG, ExistingPeriodicWorkPolicy.UPDATE,
            // An hourly task to call UpdateWorker.doWork
            PeriodicWorkRequestBuilder<UpdateWorker>(1L, TimeUnit.HOURS).build()
        )
    }.onFailure(logException).getOrNull().debugAssertNotNull
}


private fun scheduleThreeHoursAlarmManager(context: Context) {
    context.getSystemService<AlarmManager>()?.setInexactRepeating(
        AlarmManager.RTC,
        System.currentTimeMillis() + 30.minutes.inWholeMilliseconds,
        3.hours.inWholeMilliseconds,
        PendingIntent.getBroadcast(
            context,
            THREE_HOURS_INEXACT_ALARM_CODE,
            Intent(context, BroadcastReceivers::class.java)
                .setAction(BROADCAST_UPDATE_APP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        ),
    )
}
