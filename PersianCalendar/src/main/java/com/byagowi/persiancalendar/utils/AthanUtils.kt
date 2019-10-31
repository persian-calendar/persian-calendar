package com.byagowi.persiancalendar.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.ui.AthanActivity

// https://stackoverflow.com/a/27788209
fun getDefaultAthanUri(context: Context): Uri =
    (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
            context.resources.getResourcePackageName(R.raw.abdulbasit) + "/" +
            context.resources.getResourceTypeName(R.raw.abdulbasit) + "/" +
            context.resources.getResourceEntryName(R.raw.abdulbasit)).toUri()

fun getAthanVolume(context: Context): Int =
    PreferenceManager.getDefaultSharedPreferences(context)?.getInt(
        PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME
    ) ?: DEFAULT_ATHAN_VOLUME

fun isAscendingAthanVolumeEnabled(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context)?.getBoolean(
        PREF_ASCENDING_ATHAN_VOLUME, false
    ) ?: false

fun getCustomAthanUri(context: Context): Uri? =
    PreferenceManager.getDefaultSharedPreferences(context)?.getString(PREF_ATHAN_URI, "")
        ?.run { if (this.isEmpty()) null else this.toUri() }

fun startAthan(context: Context, prayTimeKey: String): Any? = if (notificationAthan) {
    context.startService(
        Intent(context, AthanNotification::class.java)
            .putExtra(KEY_EXTRA_PRAYER_KEY, prayTimeKey)
    )
} else {
    context.startActivity(
        Intent(context, AthanActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(KEY_EXTRA_PRAYER_KEY, prayTimeKey)
    )
}