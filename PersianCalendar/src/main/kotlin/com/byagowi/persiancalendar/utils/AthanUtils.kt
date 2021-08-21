package com.byagowi.persiancalendar.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.PREF_ASCENDING_ATHAN_VOLUME
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.ui.AthanActivity
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
fun startAthan(context: Context, prayTimeKey: String, intendedTime: Long) {
    // if alarm is off by 5 minutes, just skip
    if (abs(Calendar.getInstance().timeInMillis - intendedTime) > fifteenMinutesInMillis) return

    // If at the of being is disabled by user, skip
    if (prayTimeKey !in getEnabledAlarms(context)) return

    // skips if already called through either WorkManager or AlarmManager
    val today = Jdn.today
    if (lastAthanJdn == today && lastAthanKey == prayTimeKey) return
    lastAthanJdn = today; lastAthanKey = prayTimeKey

    startAthanBody(context, prayTimeKey)
}

fun startAthanBody(context: Context, prayTimeKey: String) {
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
}
