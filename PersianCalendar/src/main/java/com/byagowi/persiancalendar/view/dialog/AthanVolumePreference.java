package com.byagowi.persiancalendar.view.dialog;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

public class AthanVolumePreference extends DialogPreference {
    private Context context;
    private Utils utils;

    public AthanVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        utils = Utils.getInstance(context);
        setDialogLayoutResource(R.layout.preference_volume);
        setDialogIcon(null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        utils.prepareShapePreference(holder);
    }

    public void setVolume(float volume) {
        final boolean wasBlocking = shouldDisableDependents();
        persistFloat(volume);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    public float getVolume() {
        return getPersistedFloat(1);
    }
}
