package com.byagowi.persiancalendar.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.ui.AthanActivity

// https://stackoverflow.com/a/27788209
fun getDefaultAthanUri(context: Context): Uri {
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.resources.getResourcePackageName(R.raw.abdulbasit) + '/'.toString() +
                context.resources.getResourceTypeName(R.raw.abdulbasit) + '/'.toString() +
                context.resources.getResourceEntryName(R.raw.abdulbasit)
    )
}

fun getAthanVolume(context: Context): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    return prefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)
}

fun isAscendingAthanVolumeEnabled(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean(PREF_ASCENDING_ATHAN_VOLUME, true)
}

fun getCustomAthanUri(context: Context): Uri? {
    val uri = PreferenceManager.getDefaultSharedPreferences(context)
        .getString(PREF_ATHAN_URI, "")
    return if (TextUtils.isEmpty(uri)) null else Uri.parse(uri)
}

fun startAthan(context: Context, prayTimeKey: String) {
    if (notificationAthan) {
        // Is this needed?
        //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        //                ContextCompat.startForegroundService(context,
        //                        new Intent(context, AthanNotification.class));

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
}