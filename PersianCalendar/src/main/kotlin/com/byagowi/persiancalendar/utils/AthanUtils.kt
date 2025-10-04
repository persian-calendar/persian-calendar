package com.byagowi.persiancalendar.utils

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RawRes
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.notificationAthan
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.ui.athan.AthanActivity
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Extended Athan alarm utilities.
 *
 * Features:
 * - Snooze Athan
 * - Cancel all alarms
 * - Next upcoming Athan time
 * - Debug logging utility
 * - Reschedule missed alarms
 * - Toggle alarms dynamically
 */

fun Resources.getRawUri(@RawRes rawRes: Int) = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE,
    this.getResourcePackageName(rawRes),
    this.getResourceTypeName(rawRes),
    this.getResourceEntryName(rawRes)
)

fun getAthanUri(context: Context): Uri =
    (context.preferences.getString(PREF_ATHAN_URI, null)?.takeIf { it.isNotEmpty() }
        ?: context.resources.getRawUri(R.raw.special)).toUri()

fun startAthan(context: Context, prayTime: PrayTime, intendedTime: Long?) {
    debugLog("Alarms: startAthan for $prayTime")

    if (intendedTime == null) return startAthanBody(context, prayTime)
    if (abs(System.currentTimeMillis() - intendedTime).milliseconds > 15.minutes) return
    if (prayTime !in getEnabledAlarms(context)) return

    val preferences = context.preferences
    val lastPlayedAthanKey = preferences.getString(LAST_PLAYED_ATHAN_KEY, null)
    val lastPlayedAthanJdn = preferences.getJdnOrNull(LAST_PLAYED_ATHAN_JDN)
    val today = Jdn.today()
    if (lastPlayedAthanJdn == today && lastPlayedAthanKey == prayTime.name) return

    preferences.edit {
        putString(LAST_PLAYED_ATHAN_KEY, prayTime.name)
        putJdn(LAST_PLAYED_ATHAN_JDN, today)
    }

    startAthanBody(context, prayTime)
}

private fun startAthanBody(context: Context, prayTime: PrayTime) {
    runCatching {
        debugLog("Alarms: startAthanBody for $prayTime")
        runCatching {
            context.getSystemService<PowerManager>()?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "persiancalendar:alarm"
            )?.acquire(30.seconds.inWholeMilliseconds)
        }.onFailure(logException)

        val canPostNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        if (notificationAthan.value || canPostNotifications) {
            val intent = Intent(context, AthanNotification::class.java).putExtra(KEY_EXTRA_PRAYER, prayTime.name)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
                else context.startService(intent)
            } catch (e: Exception) {
                logException(e)
                startAthanActivity(context, prayTime)
            }
        } else {
            startAthanActivity(context, prayTime)
        }
    }.onFailure(logException)
}

fun startAthanActivity(context: Context, prayTime: PrayTime?) {
    try {
        val intent = Intent(context, AthanActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(KEY_EXTRA_PRAYER, prayTime?.name)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        logException(e)
    }
}

fun getEnabledAlarms(context: Context): Set<PrayTime> {
    if (coordinates.value == null) return emptySet()
    return (context.preferences.getString(PREF_ATHAN_ALARM, null)?.trim() ?: return emptySet())
        .splitFilterNotEmpty(",")
        .mapNotNull { PrayTime.fromName(it) }
        .toSet()
}

fun scheduleAlarms(context: Context) {
    val enabledAlarms = getEnabledAlarms(context).takeIf { it.isNotEmpty() } ?: return
    val athanGap = (context.preferences.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull() ?: .0).minutes.inWholeMilliseconds

    val prayTimes = coordinates.value?.calculatePrayTimes() ?: return

    enabledAlarms.toList().forEachIndexed { i, prayTime ->
        runCatching {
            val baseCalendar = GregorianCalendar().apply {
                set(GregorianCalendar.HOUR_OF_DAY, 0)
                set(GregorianCalendar.MINUTE, 0)
                set(GregorianCalendar.SECOND, 0)
                set(GregorianCalendar.MILLISECOND, 0)
                timeInMillis += prayTimes[prayTime].toMillis()
            }
            scheduleAlarm(context, prayTime, baseCalendar.timeInMillis - athanGap, i)
        }.onFailure(logException)
    }
}

private fun scheduleAlarm(context: Context, prayTime: PrayTime, timeInMillis: Long, i: Int) {
    val remainedMillis = timeInMillis - System.currentTimeMillis()
    debugLog("Alarms: $prayTime in ${remainedMillis / 60000} minutes")
    if (remainedMillis < 0) return

    runCatching {
        val workerInputData = Data.Builder()
            .putLong(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .putString(KEY_EXTRA_PRAYER, prayTime.name)
            .build()
        val alarmWorker = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(remainedMillis, TimeUnit.MILLISECONDS)
            .setInputData(workerInputData)
            .build()
        WorkManager.getInstance(context)
            .beginUniqueWork("$ALARM_TAG_${prayTime.name}_$i", ExistingWorkPolicy.REPLACE, alarmWorker)
            .enqueue()
    }.onFailure(logException)

    try {
        val am = context.getSystemService<AlarmManager>() ?: return
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pi = PendingIntent.getBroadcast(
            context,
            ALARMS_BASE_ID + i,
            Intent(context, BroadcastReceivers::class.java).apply {
                putExtra(KEY_EXTRA_PRAYER, prayTime.name)
                putExtra(KEY_EXTRA_PRAYER_TIME, timeInMillis)
                action = BROADCAST_ALARM
            },
            flags
        )

        if (AlarmManagerCompat.canScheduleExactAlarms(am)) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, timeInMillis, pi)
        } else {
            AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, timeInMillis, pi)
        }
    } catch (e: Exception) {
        logException(e)
    }
}

// Snooze current Athan
fun snoozeAthan(context: Context, prayTime: PrayTime, snoozeMinutes: Int) {
    val snoozeTime = System.currentTimeMillis() + snoozeMinutes.minutes.inWholeMilliseconds
    scheduleAlarm(context, prayTime, snoozeTime, 9999)
    debugLog("Snoozed $prayTime for $snoozeMinutes minutes")
}

// Cancel all scheduled alarms
fun cancelAllAlarms(context: Context) {
    try {
        val am = context.getSystemService<AlarmManager>() ?: return
        for (i in 0..10) {
            val pi = PendingIntent.getBroadcast(
                context,
                ALARMS_BASE_ID + i,
                Intent(context, BroadcastReceivers::class.java),
                PendingIntent.FLAG_NO_CREATE or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )
            pi?.let { am.cancel(it) }
        }
        debugLog("All alarms cancelled")
    } catch (e: Exception) {
        logException(e)
    }
}

// Get next upcoming Athan
fun getNextAthanTime(context: Context): Pair<PrayTime, Long>? {
    val prayTimes = coordinates.value?.calculatePrayTimes() ?: return null
    val now = System.currentTimeMillis()
    return prayTimes.entries.map { it.key to it.value.toMillis() }
        .map { (prayTime, millis) -> prayTime to GregorianCalendar().apply {
            set(GregorianCalendar.HOUR_OF_DAY, 0)
            set(GregorianCalendar.MINUTE, 0)
            set(GregorianCalendar.SECOND, 0)
            set(GregorianCalendar.MILLISECOND, 0)
            timeInMillis += millis
        }.timeInMillis }
        .filter { it.second > now }
        .minByOrNull { it.second }
}

// Reschedule missed alarms
fun rescheduleMissedAlarms(context: Context) {
    val enabledAlarms = getEnabledAlarms(context)
    val prayTimes = coordinates.value?.calculatePrayTimes() ?: return
    val now = System.currentTimeMillis()

    enabledAlarms.forEachIndexed { i, prayTime ->
        val baseCalendar = GregorianCalendar().apply {
            set(GregorianCalendar.HOUR_OF_DAY, 0)
            set(GregorianCalendar.MINUTE, 0)
            set(GregorianCalendar.SECOND, 0)
            set(GregorianCalendar.MILLISECOND, 0)
            timeInMillis += prayTimes[prayTime].toMillis()
        }
        if (baseCalendar.timeInMillis < now) {
            debugLog("Rescheduling missed alarm for $prayTime")
            scheduleAlarm(context, prayTime, baseCalendar.timeInMillis + 24.hours.inWholeMilliseconds, i)
        }
    }
}

// Toggle alarm state
fun toggleAthanAlarm(context: Context, prayTime: PrayTime, enable: Boolean) {
    val current = getEnabledAlarms(context).toMutableSet()
    if (enable) current.add(prayTime) else current.remove(prayTime)
    context.preferences.edit { putString(PREF_ATHAN_ALARM, current.joinToString(",") { it.name }) }
    scheduleAlarms(context)
    debugLog("Alarm for $prayTime ${if (enable) "enabled" else "disabled"}")
}

// Debug log
fun debugLog(message: String) {
    println("[DEBUG] $message")
}
 
