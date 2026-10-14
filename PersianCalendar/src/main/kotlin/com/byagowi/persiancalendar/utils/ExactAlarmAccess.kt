package com.byagowi.persiancalendar.utils

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.core.net.toUri

internal fun canScheduleExactAlarms(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || runCatching {
        context.getSystemService<AlarmManager>()?.canScheduleExactAlarms() == true
    }.onFailure(logException).getOrDefault(false)

internal fun requestExactAlarmAccess(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    // This is special app access, not a runtime permission. Settings has no grant result.
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData("package:${context.packageName}".toUri()),
        )
    }.onFailure(logException).onFailure { showUnsupportedActionToast(context) }
}

internal fun rescheduleAlarmsWithExactAccess(context: Context) {
    // Recheck even after the grant broadcast: access may already have been revoked.
    if (!canScheduleExactAlarms(context)) return
    // Do not use update(false): its same-day cache skips scheduling after a grant.
    runCatching { scheduleAlarms(context.applicationContext) }.onFailure(logException)
}
