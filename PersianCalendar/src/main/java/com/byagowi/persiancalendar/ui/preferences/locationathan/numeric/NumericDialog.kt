package com.byagowi.persiancalendar.ui.preferences.locationathan.numeric

import android.os.Build
import android.text.InputType
import android.view.View
import android.widget.EditText

import androidx.preference.EditTextPreferenceDialogFragmentCompat

/**
 * Created by ebraminio on 2/21/16.
 */
class NumericDialog : EditTextPreferenceDialogFragmentCompat() {
    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        view?.findViewById<EditText>(android.R.id.edit)?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

            // on platforms supporting direction as LTR direction is more handy on editing numbers
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textDirection = View.TEXT_DIRECTION_LTR
                layoutDirection = View.LAYOUT_DIRECTION_LTR
            }
        }
    }
}
