package com.byagowi.persiancalendar.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.ALARMS_BASE_ID
import com.byagowi.persiancalendar.ALARM_TAG
import com.byagowi.persiancalendar.BROADCAST_ALARM
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_TIME
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.logDebug
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.ui.AthanActivity
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// https://stackoverflow.com/a/27788209
fun getDefaultAthanUri(context: Context) = "%s://%s/%s/%s".format(
    ContentResolver.SCHEME_ANDROID_RESOURCE,
    context.resources.getResourcePackageName(R.raw.abdulbasit),
    context.resources.getResourceTypeName(R.raw.abdulbasit),
    context.resources.getResourceEntryName(R.raw.abdulbasit)
).toUri()

val Context.athanVolume: Int get() = appPrefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)

val Context.isAscendingAthanVolumeEnabled: Boolean
    get() = appPrefs.getBoolean(PREF_ASCENDING_ATHAN_VOLUME, false)

fun getCustomAthanUri(context: Context): Uri? =
    context.appPrefs.getString(PREF_ATHAN_URI, null)?.takeIf { it.isNotEmpty() }?.toUri()

private val fifteenMinutesInMillis = TimeUnit.MINUTES.toMillis(15)
private var lastAthanKey = ""
private var lastAthanJdn: Jdn? = null
fun startAthan(context: Context, prayTimeKey: String, intendedTime: Long?) {
    logDebug("Alarms", "startAthan for $prayTimeKey")
    if (intendedTime == null) return startAthanBody(context, prayTimeKey)
    // if alarm is off by 5 minutes, just skip
    if (abs(System.currentTimeMillis() - intendedTime) > fifteenMinutesInMillis) return

    // If at the of being is disabled by user, skip
    if (prayTimeKey !in getEnabledAlarms(context)) return

    // skips if already called through either WorkManager or AlarmManager
    val today = Jdn.today
    if (lastAthanJdn == today && lastAthanKey == prayTimeKey) return
    lastAthanJdn = today; lastAthanKey = prayTimeKey

    startAthanBody(context, prayTimeKey)
}

private fun startAthanBody(context: Context, prayTimeKey: String) = runCatching {
    logDebug("Alarms", "startAthanBody for $prayTimeKey")
    if (notificationAthan) {
        context.startService(
            Intent(context, AthanNotification::class.java)
                .putExtra(KEY_EXTRA_PRAYER, prayTimeKey)
        )
    } else {
        context.startActivity(
            Intent(context, AthanActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(KEY_EXTRA_PRAYER, prayTimeKey)
        )
    }
}.onFailure(logException).let {}

fun getEnabledAlarms(context: Context): Set<String> {
    if (coordinates == null) return emptySet()
    return (context.appPrefs.getString(PREF_ATHAN_ALARM, null)?.trim() ?: return emptySet())
        .splitIgnoreEmpty(",")
        .toSet()
}

fun scheduleAlarms(context: Context) {
    val enabledAlarms = getEnabledAlarms(context).takeIf { it.isNotEmpty() } ?: return
    val athanGap =
        ((context.appPrefs.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull()
            ?: .0) * 60.0 * 1000.0).toLong()

    val prayTimes = PrayTimesCalculator.calculate(calculationMethod, Date(), coordinates)
    // convert spacedComma separated string to a set
    enabledAlarms.forEachIndexed { i, name ->
        val alarmTime: Clock = when (name) {
            "DHUHR" -> prayTimes.dhuhrClock
            "ASR" -> prayTimes.asrClock
            "MAGHRIB" -> prayTimes.maghribClock
            "ISHA" -> prayTimes.ishaClock
            "FAJR" -> prayTimes.fajrClock
            // a better to have default
            else -> prayTimes.fajrClock
        }

        scheduleAlarm(context, name, Calendar.getInstance().also {
            it.set(Calendar.HOUR_OF_DAY, alarmTime.hour)
            it.set(Calendar.MINUTE, alarmTime.minute)
            it.set(Calendar.SECOND, 0)
        }.timeInMillis - athanGap, i)
    }
}

private fun scheduleAlarm(context: Context, alarmTimeName: String, timeInMillis: Long, i: Int) {
    val remainedMillis = timeInMillis - System.currentTimeMillis()
    logDebug("Alarms", "$alarmTimeName in ${remainedMillis / 60000} minutes")
    if (remainedMillis < 0) return // Don't set alarm in past

    if (enableWorkManager) { // Schedule in both, startAthan has the logic to skip duplicated calls
        val workerInputData = Data.Builder().putLong(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .putString(KEY_EXTRA_PRAYER, alarmTimeName).build()
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
            .putExtra(KEY_EXTRA_PRAYER, alarmTimeName)
            .putExtra(KEY_EXTRA_PRAYER_TIME, timeInMillis)
            .setAction(BROADCAST_ALARM),
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )
    when {
        Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 ->
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 ->
            am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        else -> am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}

@StringRes
fun getPrayTimeText(athanKey: String?): Int = when (athanKey) {
    "FAJR" -> R.string.fajr
    "DHUHR" -> R.string.dhuhr
    "ASR" -> R.string.asr
    "MAGHRIB" -> R.string.maghrib
    "ISHA" -> R.string.isha
    else -> R.string.isha
}

@DrawableRes
fun getPrayTimeImage(athanKey: String?): Int = when (athanKey) {
    "FAJR" -> R.drawable.fajr
    "DHUHR" -> R.drawable.dhuhr
    "ASR" -> R.drawable.asr
    "MAGHRIB" -> R.drawable.maghrib
    "ISHA" -> R.drawable.isha
    else -> R.drawable.isha
}
