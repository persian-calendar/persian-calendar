package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.byagowi.persiancalendar.util.Utils;

/**
 * Created by ebraminio on 2/16/16.
 */
public class ShapedCheckBoxPreference extends CheckBoxPreference {
    public ShapedCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShapedCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShapedCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShapedCheckBoxPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Utils.getInstance(getContext()).setFontAndShape(holder);
    }
}
