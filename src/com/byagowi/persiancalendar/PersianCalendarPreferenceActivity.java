package com.byagowi.persiancalendar;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PersianCalendarPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }   
}