package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.splitIgnoreEmpty
import com.byagowi.persiancalendar.utils.startAthan

fun showPrayerSelectDialog(context: Context) {
    val entriesKeys = context.resources.getStringArray(R.array.prayerTimeKeys)
    val alarms = (context.appPrefs.getString(PREF_ATHAN_ALARM, null) ?: "")
        .splitIgnoreEmpty(",").toMutableSet()
    val checked = entriesKeys.map { it in alarms }.toBooleanArray()

    AlertDialog.Builder(context)
        .setTitle(R.string.athan_alarm)
        .setMultiChoiceItems(R.array.prayerTimeNames, checked) { _, which, isChecked ->
            val key = entriesKeys[which].toString()
            if (isChecked) alarms.add(key) else alarms.remove(key)
        }
        .setPositiveButton(R.string.accept) { _, _ ->
            context.appPrefs.edit { putString(PREF_ATHAN_ALARM, alarms.joinToString(",")) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

fun showPrayerSelectPreviewDialog(context: Context) {
    AlertDialog.Builder(context)
        .setTitle(R.string.athan)
        .setItems(R.array.prayerTimeNames) { _, which ->
            val entriesKeys = context.resources.getStringArray(R.array.prayerTimeKeys)
            startAthan(context, entriesKeys.getOrNull(which) ?: "FAJR")
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
