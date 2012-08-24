/*
 * March 2012
 *
 * In place of a legal notice, here is a blessing:
 *
 *    May you do good and not evil.
 *    May you find forgiveness for yourself and forgive others.
 *    May you share freely, never taking more than you give.
 *
 */
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