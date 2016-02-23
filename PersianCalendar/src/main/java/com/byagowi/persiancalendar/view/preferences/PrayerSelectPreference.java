package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.byagowi.persiancalendar.util.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PrayerSelectPreference extends DialogPreference {
    public PrayerSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPrayers(Set<String> prayers) {
        final boolean wasBlocking = shouldDisableDependents();
        persistString(TextUtils.join(",", prayers));
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    public Set<String> getPrayers() {
        Set<String> result = new HashSet<>();
        result.addAll(Arrays.asList(TextUtils.split(getPersistedString(""), ",")));
        return result;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Utils.getInstance(getContext()).prepareShapePreference(holder);
    }
}
