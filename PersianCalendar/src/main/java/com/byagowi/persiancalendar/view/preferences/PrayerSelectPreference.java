package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import androidx.preference.DialogPreference;

public class PrayerSelectPreference extends DialogPreference {

    public PrayerSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPrayers(Set<String> prayers) {
        final boolean wasBlocking = shouldDisableDependents();
        // convert set to a comma separated string
        persistString(TextUtils.join(",", prayers));
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    public Set<String> getPrayers() {
        // convert comma separated string to a set
        return new HashSet<>(Arrays.asList(TextUtils.split(getPersistedString(""), ",")));
    }

}
