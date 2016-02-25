package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

public class AthanVolumePreference extends DialogPreference {
    private Utils utils;

    public AthanVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        utils = Utils.getInstance(context);
        setDialogLayoutResource(R.layout.preference_volume);
        setDialogIcon(null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        utils.setFontAndShape(holder);
    }

    public void setVolume(int volume) {
        final boolean wasBlocking = shouldDisableDependents();
        persistInt(volume);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    public int getVolume() {
        return getPersistedInt(1);
    }
}
