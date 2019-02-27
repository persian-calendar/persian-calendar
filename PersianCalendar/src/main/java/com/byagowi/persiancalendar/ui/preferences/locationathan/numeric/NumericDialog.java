package com.byagowi.persiancalendar.ui.preferences.locationathan.numeric;

import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.preference.EditTextPreferenceDialogFragmentCompat;

/**
 * Created by ebraminio on 2/21/16.
 */
public class NumericDialog extends EditTextPreferenceDialogFragmentCompat {

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        EditText editText = view.findViewById(android.R.id.edit);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |
                InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // on platforms supporting direction as LTR direction is more handy on editing numbers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            editText.setTextDirection(View.TEXT_DIRECTION_LTR);
            editText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }
}
