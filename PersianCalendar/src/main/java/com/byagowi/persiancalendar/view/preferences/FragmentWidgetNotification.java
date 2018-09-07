package com.byagowi.persiancalendar.view.preferences;

import android.os.Bundle;

import com.byagowi.persiancalendar.R;

import androidx.preference.PreferenceFragmentCompat;

public class FragmentWidgetNotification extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences_widget_notification);
    }
}
