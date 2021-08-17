package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.splitIgnoreEmpty
import com.byagowi.persiancalendar.utils.startAthan

private val prayerTimesNames = listOf(
    R.string.fajr, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.isha
)
private val prayerTimesKeys = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")

fun showPrayerSelectDialog(context: Context) {
    val alarms = (context.appPrefs.getString(PREF_ATHAN_ALARM, null) ?: "")
        .splitIgnoreEmpty(",").toMutableSet()
    val checked = prayerTimesKeys.map { it in alarms }.toBooleanArray()

    val prayerTimesNames = prayerTimesNames.map(context::getString).toTypedArray()
    AlertDialog.Builder(context)
        .setTitle(R.string.athan_alarm)
        .setMultiChoiceItems(prayerTimesNames, checked) { _, which, isChecked ->
            val key = prayerTimesKeys[which]
            if (isChecked) alarms.add(key) else alarms.remove(key)
        }
        .setPositiveButton(R.string.accept) { _, _ ->
            context.appPrefs.edit { putString(PREF_ATHAN_ALARM, alarms.joinToString(",")) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

fun showPrayerSelectPreviewDialog(context: Context) {
    val prayerTimesNames = prayerTimesNames.map(context::getString).toTypedArray()
    AlertDialog.Builder(context)
        .setTitle(R.string.athan)
        .setItems(prayerTimesNames) { _, which ->
            startAthan(context, prayerTimesKeys.getOrNull(which) ?: "FAJR")
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
