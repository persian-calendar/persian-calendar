package com.byagowi.persiancalendar.ui.settings.locationathan.location

import android.location.Geocoder
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DialogCoordinatesBinding
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.friendlyName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.saveLocation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

fun showCoordinatesDialog(
    activity: FragmentActivity,
    viewLifecycleOwner: LifecycleOwner,
    inputCoordinates: Coordinates? = null
) {
    val coordinates = inputCoordinates ?: coordinates
    val binding = DialogCoordinatesBinding.inflate(activity.layoutInflater)

    val coordinatesEdits = listOf(binding.latitude, binding.longitude, binding.altitude)
    val coordinatesKeys = listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE)

    coordinatesEdits.zip(
        coordinates?.run { listOf(latitude, longitude, elevation) } ?: listOf(.0, .0, .0)
    ) { editable, value -> editable.setText(value.toString()) }

    var cityName: String? = null
    var countryCode: String? = null
    fun showGeocoderResult() {
        val latitude = binding.latitude.text?.toString()?.toDoubleOrNull() ?: 0.0
        val longitude = binding.longitude.text?.toString()?.toDoubleOrNull() ?: 0.0
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(activity, language.asSystemLocale())
                    .getFromLocation(latitude, longitude, 20)
                withContext(Dispatchers.Main.immediate) {
                    val result = geocoder.getOrNull(0)
                    cityName = result?.friendlyName
                    countryCode = result?.countryCode
                    binding.geocoder.text = cityName ?: ""
                    binding.geocoder.isVisible = !cityName.isNullOrBlank()
                }
            }.onFailure(logException)
        }
    }
    if (inputCoordinates != null) showGeocoderResult()

    binding.latitude.addTextChangedListener { showGeocoderResult() }
    binding.longitude.addTextChangedListener { showGeocoderResult() }

    val dialogBuilder = MaterialAlertDialogBuilder(activity)
        .setView(binding.root)
        .setTitle(R.string.coordination)
        .setPositiveButton(R.string.accept) { _, _ ->
            val parts = coordinatesEdits.map { it.text.toString() }.mapIndexed { i, x ->
                // Replace empty elevation with zero
                if (i == 2 && x.isEmpty()) "0" else x
            }.mapIndexedNotNull { i, coordinate ->
                // Make sure coordinates array has both parsable and in range numbers
                coordinate.toDoubleOrNull()?.takeIf {
                    when (i) {
                        0 -> abs(it) <= 90 // Valid latitudes
                        1 -> abs(it) <= 180 // Valid longitudes
                        else -> it in -418.0..848.0 // Altitude, from Dead Sea to Mount Everest
                    }
                }
            }
            if (parts.size == 3) activity.appPrefs.saveLocation(
                Coordinates(parts[0], parts[1], parts[2]), cityName ?: "", countryCode ?: ""
            ) else activity.appPrefs.edit { coordinatesKeys.forEach(::remove) }
        }
        .setNegativeButton(R.string.cancel, null)

    var dialog: AlertDialog? = null
    if (inputCoordinates == null) dialogBuilder.setNeutralButton(R.string.map) { _, _ ->
        dialog?.dismiss()
        activity.findNavController(R.id.navHostFragment).navigate(R.id.map)
    }
    dialog = dialogBuilder.create().also { it.show() }
}
