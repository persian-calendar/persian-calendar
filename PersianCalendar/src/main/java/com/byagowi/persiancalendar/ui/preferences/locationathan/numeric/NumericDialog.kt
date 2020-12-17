package com.byagowi.persiancalendar.ui.preferences.locationathan.numeric

import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.preference.EditTextPreferenceDialogFragmentCompat

class NumericDialog : EditTextPreferenceDialogFragmentCompat() {
    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        view?.findViewById<EditText>(android.R.id.edit)?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            // LTR direction is more handy on editing numbers
            textDirection = View.TEXT_DIRECTION_LTR
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }
}
