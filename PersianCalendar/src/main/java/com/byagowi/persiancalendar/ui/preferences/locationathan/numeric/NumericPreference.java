package com.byagowi.persiancalendar.ui.preferences.locationathan.numeric;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

/**
 * Created by ebraminio on 2/16/16.
 */
public class NumericPreference extends EditTextPreference {

    private Double mDouble;

    public NumericPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NumericPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NumericPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumericPreference(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return mDouble != null ? mDouble.toString() : null;
    }

    // http://stackoverflow.com/a/10848393
    @Override
    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        mDouble = parseDouble(text);
        persistString(mDouble != null ? mDouble.toString() : null);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    private Double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
