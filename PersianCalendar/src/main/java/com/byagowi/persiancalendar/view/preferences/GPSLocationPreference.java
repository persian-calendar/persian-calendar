package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.byagowi.persiancalendar.util.Utils;

/**
 * Created by ebrahim on 3/26/16.
 */
public class GPSLocationPreference extends DialogPreference {

    public GPSLocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Utils.getInstance(getContext()).setFontAndShape(holder);
    }

}
