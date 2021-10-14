package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DialogCoordinatesBinding
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.utils.appPrefs

fun showCoordinatesDialog(activity: Activity) {
    val binding = DialogCoordinatesBinding.inflate(activity.layoutInflater)

    // As we don't already a string concatenated of the two, let's do in code
    binding.altitudeLabel.hint = listOf(R.string.altitude, R.string.altitude_praytime)
        .joinToString(spacedComma) { activity.getString(it) }

    val coordinatesEdits = listOf(binding.latitude, binding.longitude, binding.altitude)
    val coordinatesKeys = listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE)

    coordinatesEdits.zip(
        coordinates?.run { listOf(latitude, longitude, elevation) } ?: listOf(.0, .0, .0)
    ) { editable, value -> editable.setText(value.toString()) }

    AlertDialog.Builder(activity)
        .setView(binding.root)
        .setTitle(R.string.coordination)
        .setPositiveButton(R.string.accept) { _, _ ->
            val coordinates = coordinatesEdits.map { it.text.toString() }.mapIndexed { i, x ->
                // Replace empty elevation with zero
                if (i == 2 && x.isEmpty()) "0" else x
            }
            // just ensure they are parsable numbers for the last time otherwise reset it
            if (coordinates.all { it.toDoubleOrNull() != null })
                activity.appPrefs.edit { coordinatesKeys.zip(coordinates, ::putString) }
            else
                activity.appPrefs.edit { coordinatesKeys.forEach(::remove) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
