package com.byagowi.persiancalendar.ui.preferences.locationathan.coordinates

import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceManager
import com.byagowi.persiancalendar.R

class CoordinatesDialog : PreferenceDialogFragmentCompat() {

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)

        builder?.setView(R.layout.dialog_coordinates)?.create()
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        dialog?.findViewById<EditText>(R.id.coordinate_latitude)?.apply {
            setText(sharedPreferences.getString("Latitude", "0.0"))
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            textDirection = View.TEXT_DIRECTION_LTR
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        dialog?.findViewById<EditText>(R.id.coordinate_longitude)?.apply {
            setText(sharedPreferences.getString("Longitude", "0.0"))
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            textDirection = View.TEXT_DIRECTION_LTR
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        dialog?.findViewById<EditText>(R.id.coordinate_altitude)?.apply {
            setText(sharedPreferences.getString("Altitude", "0.0"))
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            textDirection = View.TEXT_DIRECTION_LTR
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (positiveResult) {
            sharedPreferences.edit()
                .putString("Latitude", dialog?.findViewById<EditText>(R.id.coordinate_latitude)?.text.toString())
                .putString("Longitude", dialog?.findViewById<EditText>(R.id.coordinate_longitude)?.text.toString())
                .putString("Altitude", dialog?.findViewById<EditText>(R.id.coordinate_altitude)?.text.toString())
                .apply()
        }
    }
}
