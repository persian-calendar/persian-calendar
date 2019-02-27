package com.byagowi.persiancalendar.ui.preferences.locationathan.athan;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class AthanVolumePreference extends DialogPreference {

    public AthanVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogIcon(null);
    }

    int getVolume() {
        return getPersistedInt(1);
    }

    void setVolume(int volume) {
        final boolean wasBlocking = shouldDisableDependents();
        persistInt(volume);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }
}
