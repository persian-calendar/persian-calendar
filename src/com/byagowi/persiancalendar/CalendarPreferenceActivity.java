package com.byagowi.persiancalendar;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * Preference activity
 * 
 * @author ebraminio
 */
public class CalendarPreferenceActivity extends PreferenceActivity {
	public CalendarUtils utils = CalendarUtils.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		utils.setTheme(this);
		super.onCreate(savedInstanceState);

	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
	    	lowerThanHC();
	    } else {
	    	higherThanHC();
	    }
	}

	@SuppressWarnings("deprecation")
	private void lowerThanHC() {
    	addPreferencesFromResource(R.xml.preferences);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void higherThanHC() {
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefFragment())
				.commit();
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class PrefFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}
}