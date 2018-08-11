package com.byagowi.persiancalendar.view.preferences

import android.os.Build
import android.text.InputType
import android.view.View
import android.widget.EditText

import androidx.preference.EditTextPreferenceDialogFragmentCompat

/**
 * Created by ebraminio on 2/21/16.
 */
class AthanNumericDialog : EditTextPreferenceDialogFragmentCompat() {

  override fun onBindDialogView(view: View) {
    super.onBindDialogView(view)

    val editText = view.findViewById<EditText>(android.R.id.edit)
    editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
        InputType.TYPE_NUMBER_FLAG_DECIMAL

    // on platforms supporting direction as LTR direction is more handy on editing numbers
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      editText.textDirection = View.TEXT_DIRECTION_LTR
      editText.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }
  }
}
