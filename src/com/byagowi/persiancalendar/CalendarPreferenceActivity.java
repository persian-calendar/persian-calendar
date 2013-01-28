package com.byagowi.persiancalendar;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Preference activity
 * 
 * @author ebraminio
 *
 */
public class CalendarPreferenceActivity extends PreferenceActivity {
	@Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}