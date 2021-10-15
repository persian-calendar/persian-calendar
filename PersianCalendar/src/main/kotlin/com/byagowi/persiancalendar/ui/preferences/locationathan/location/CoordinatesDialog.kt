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
import com.byagowi.persiancalendar.utils.appPrefs
import kotlin.math.abs

fun showCoordinatesDialog(activity: Activity) {
    val binding = DialogCoordinatesBinding.inflate(activity.layoutInflater)

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

            // Make sure coordinates array has both parsable and in range numbers
            val isValidCoordinates = coordinates.mapIndexedNotNull { i, coordinate ->
                coordinate.toDoubleOrNull()?.takeIf {
                    when (i) {
                        0 -> abs(it) <= 90 // Valid latitudes
                        1 -> abs(it) <= 180 // Valid longitudes
                        else -> it in -418.0..848.0 // Altitude, from Dead Sea to Mount Everest
                    }
                }
            }.size == 3

            if (isValidCoordinates)
                activity.appPrefs.edit { coordinatesKeys.zip(coordinates, ::putString) }
            else
                activity.appPrefs.edit { coordinatesKeys.forEach(::remove) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
