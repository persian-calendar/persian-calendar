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
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.ALARMS_BASE_ID
import com.byagowi.persiancalendar.ALARM_TAG
import com.byagowi.persiancalendar.BROADCAST_ALARM
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_TIME
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.notificationAthan
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.ui.athan.AthanActivity
import com.byagowi.persiancalendar.variants.debugLog
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// https://stackoverflow.com/a/69505596
fun Resources.getRawUri(@RawRes rawRes: Int) = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE, this.getResourcePackageName(rawRes),
    this.getResourceTypeName(rawRes), this.getResourceEntryName(rawRes)
)

fun getAthanUri(context: Context): Uri =
    (context.preferences.getString(PREF_ATHAN_URI, null)?.takeIf { it.isNotEmpty() }
        ?: context.resources.getRawUri(R.raw.special)).toUri()

private var lastAthanKey: PrayTime? = null
private var lastAthanJdn: Jdn? = null
fun startAthan(context: Context, prayTime: PrayTime, intendedTime: Long?) {
    debugLog("Alarms: startAthan for $prayTime")
    if (intendedTime == null) return startAthanBody(context, prayTime)
    // if alarm is off by 15 minutes, just skip
    if (abs(System.currentTimeMillis() - intendedTime) > FIFTEEN_MINUTES_IN_MILLIS) return

    // If at the of being is disabled by user, skip
    if (prayTime !in getEnabledAlarms(context)) return

    // skips if already called through either WorkManager or AlarmManager
    val today = Jdn.today()
    if (lastAthanJdn == today && lastAthanKey == prayTime) return
    lastAthanJdn = today; lastAthanKey = prayTime

    startAthanBody(context, prayTime)
}

private fun startAthanBody(context: Context, prayTime: PrayTime) {
    runCatching {
        debugLog("Alarms: startAthanBody for $prayTime")

        runCatching {
            context.getSystemService<PowerManager>()?.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,
                "persiancalendar:alarm"
            )?.acquire(THIRTY_SECONDS_IN_MILLIS)
        }.onFailure(logException)

        if (notificationAthan.value || ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) context.startService(
            Intent(context, AthanNotification::class.java)
                .putExtra(KEY_EXTRA_PRAYER, prayTime.name)
        ) else startAthanActivity(context, prayTime)
    }.onFailure(logException)
}

fun startAthanActivity(context: Context, prayTime: PrayTime?) {
    context.startActivity(
        Intent(context, AthanActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(KEY_EXTRA_PRAYER, prayTime?.name)
    )
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
    val athanGap =
        ((context.preferences.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull()
            ?: .0) * 60.0 * 1000.0).toLong()

    val prayTimes = coordinates.value?.calculatePrayTimes() ?: return
    // convert spacedComma separated string to a set
    enabledAlarms.forEachIndexed { i, prayTime ->
        scheduleAlarm(context, prayTime, GregorianCalendar().also {
            // if (name == ISHA_KEY) return@also it.add(Calendar.SECOND, 5)
            it[GregorianCalendar.HOUR_OF_DAY] = 0
            it[GregorianCalendar.MINUTE] = 0
            it[GregorianCalendar.SECOND] = 0
            it[GregorianCalendar.MILLISECOND] = 0
            it.timeInMillis += prayTimes[prayTime].toMillis()
        }.timeInMillis - athanGap, i)
    }
}

private fun scheduleAlarm(context: Context, prayTime: PrayTime, timeInMillis: Long, i: Int) {
    val remainedMillis = timeInMillis - System.currentTimeMillis()
    debugLog("Alarms: $prayTime in ${remainedMillis / 60000} minutes")
    if (remainedMillis < 0) return // Don't set alarm in past

    run { // Schedule in both alarmmanager and workmanager, startAthan has the logic to skip duplicated calls
        val workerInputData = Data.Builder().putLong(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .putString(KEY_EXTRA_PRAYER, prayTime.name).build()
        val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInitialDelay(remainedMillis, TimeUnit.MILLISECONDS)
            .setInputData(workerInputData)
            .build()
        WorkManager.getInstance(context)
            .beginUniqueWork(ALARM_TAG + i, ExistingWorkPolicy.REPLACE, alarmWorker)
            .enqueue()
    }

    val am = context.getSystemService<AlarmManager>() ?: return
    val pendingIntent = PendingIntent.getBroadcast(
        context, ALARMS_BASE_ID + i,
        Intent(context, BroadcastReceivers::class.java)
            .putExtra(KEY_EXTRA_PRAYER, prayTime.name)
            .putExtra(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .setAction(BROADCAST_ALARM),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms())
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            am, AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
        )
}
