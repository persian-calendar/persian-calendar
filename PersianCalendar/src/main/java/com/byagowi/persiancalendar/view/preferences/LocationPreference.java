package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
public class LocationPreference extends DialogPreference {

    public LocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSelected(String selected) {
        final boolean wasBlocking = shouldDisableDependents();
        persistString(selected);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }
}
