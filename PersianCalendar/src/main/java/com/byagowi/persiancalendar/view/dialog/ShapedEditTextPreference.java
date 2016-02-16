package com.byagowi.persiancalendar.view.dialog;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.byagowi.persiancalendar.Utils;

/**
 * Created by ebraminio on 2/16/16.
 */
public class ShapedEditTextPreference extends EditTextPreference {
    public ShapedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShapedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShapedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShapedEditTextPreference(Context context) {
        super(context);
    }

    Utils utils = Utils.getInstance();
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        utils.prepareShapePreference(getContext(), holder);
    }
}
