package com.byagowi.persiancalendar.ui.preferences.locationathan.coordinates

import android.text.InputType
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.PreferenceDialogFragmentCompat
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DialogCoordinatesBinding
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.spacedComma

class CoordinatesDialog : PreferenceDialogFragmentCompat() {

    private var binding: DialogCoordinatesBinding? = null

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        val context = builder?.context ?: return
        builder.setView(DialogCoordinatesBinding.inflate(context.layoutInflater).also { binding ->
            binding.coordinatesEditable.zip(
                coordinatesKeys.map { context.appPrefs.getString(it, "0.0") }
            ) { editable, value ->
                editable.setText(value)
                editable.inputType = InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
                editable.textDirection = View.TEXT_DIRECTION_LTR
                editable.layoutDirection = View.LAYOUT_DIRECTION_LTR
            }
            binding.altitudeLabel.text = listOf(R.string.altitude, R.string.altitude_praytime)
                .joinToString(spacedComma) { context.getString(it) }
            this.binding = binding
        }.root)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val context = context ?: return
            val binding = binding ?: return
            val coordinates = binding.coordinatesEditable.map { it.text.toString() }
            // just ensure they are parsable numbers, if not, bail out
            if (coordinates.any { it.toDoubleOrNull() == null }) return
            context.appPrefs.edit { coordinatesKeys.zip(coordinates, ::putString) }
        }
    }

    companion object {
        private val coordinatesKeys = listOf(PREF_LATITUDE, PREF_LONGITUDE, PREF_ALTITUDE)
        private val DialogCoordinatesBinding.coordinatesEditable
            get() = listOf(latitude, longitude, altitude)
    }
}
