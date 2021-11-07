package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getCustomAthanUri
import com.byagowi.persiancalendar.utils.getRawUri
import com.byagowi.persiancalendar.utils.logException

fun showAthanSelectDialog(activity: Activity, pickRingtone: ActivityResultLauncher<Uri?>) {
    fun selectAthan(@RawRes rawRes: Int, @StringRes titleRes: Int) {
    }

    val items = listOf(
        R.string.default_athan to callback@{
            val prefs = activity.appPrefs
            if (PREF_ATHAN_URI !in prefs && PREF_ATHAN_NAME !in prefs) return@callback
            prefs.edit { remove(PREF_ATHAN_URI); remove(PREF_ATHAN_NAME) }
            Toast.makeText(activity, R.string.returned_to_default, Toast.LENGTH_SHORT).show()
        }
    ) + listOf(
        R.string.special_voice to R.raw.special,
        R.string.abdulbasit to R.raw.abdulbasit,
        R.string.moazzenzadeh to R.raw.moazzenzadeh,
        R.string.entezar to R.raw.entezar
    ).map { (stringId, rawId) ->
        stringId to {
            val prefs = activity.appPrefs
            prefs.edit {
                putString(PREF_ATHAN_URI, activity.resources.getRawUri(rawId))
                putString(PREF_ATHAN_NAME, activity.getString(stringId))
            }
        }
    } + listOf(
        R.string.more to {
            runCatching {
                pickRingtone.launch(getCustomAthanUri(activity))
            }.onFailure(logException).getOrNull()
        }
    )
    AlertDialog.Builder(activity)
        .setTitle(R.string.custom_athan)
        .setItems(items.map { activity.getString(it.first) }.toTypedArray()) { dialog, which ->
            items[which].second()
            dialog.dismiss()
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

