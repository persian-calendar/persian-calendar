package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DialogCoordinatesBinding
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getCoordinate
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.spacedComma

fun Fragment.showCoordinatesDialog(): Boolean {
    val context = context ?: return true
    val binding = DialogCoordinatesBinding.inflate(context.layoutInflater)

    // As we don't already a string concatenated of the two, let's do in code
    binding.altitudeLabel.text = listOf(R.string.altitude, R.string.altitude_praytime)
        .joinToString(spacedComma) { context.getString(it) }

    val coordinatesEdits = listOf(binding.latitude, binding.longitude, binding.altitude)
    val coordinatesKeys = listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE)

    coordinatesEdits.zip(
        getCoordinate(context)?.let { listOf(it.latitude, it.longitude, it.elevation) }
            ?: listOf(.0, .0, .0)
    ) { editable, value -> editable.setText(value.toString()) }

    AlertDialog.Builder(context)
        .setView(binding.root)
        .setTitle(R.string.coordination)
        .setPositiveButton(R.string.accept) { _, _ ->
            val coordinates = coordinatesEdits.map { it.text.toString() }
            // just ensure they are parsable numbers for the last time otherwise reset it
            if (coordinates.all { it.toDoubleOrNull() != null })
                this.context?.appPrefs?.edit { coordinatesKeys.zip(coordinates, ::putString) }
            else
                this.context?.appPrefs?.edit { coordinatesKeys.forEach(::remove) }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()

    return true
}
