package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.byagowi.persiancalendar.util.Utils;

import java.util.Set;

public class PrayerSelectPreference extends DialogPreference {
    Utils utils;

    public PrayerSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        utils = Utils.getInstance(context);
    }

    public void setPrayers(Set<String> prayers) {
        final boolean wasBlocking = shouldDisableDependents();
        persistString(utils.setToCommaSeparated(prayers));
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    public Set<String> getPrayers() {
        return utils.commaSeparatedToSet(getPersistedString(""));
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        utils.setFontAndShape(holder);
    }
}
