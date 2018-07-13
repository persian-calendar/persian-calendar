package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import androidx.preference.DialogPreference;
import android.util.AttributeSet;

import com.byagowi.persiancalendar.R;

public class AthanVolumePreference extends DialogPreference {

    public AthanVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_volume);
        setDialogIcon(null);
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
