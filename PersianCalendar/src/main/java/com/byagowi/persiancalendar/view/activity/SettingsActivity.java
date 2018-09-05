package com.byagowi.persiancalendar.view.activity;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.byagowi.persiancalendar.R;

import java.util.List;

import androidx.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity {

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            int preferenceFile_toLoad=-1;
            String settings = getArguments().getString("settings");
            if ("prefs_general".equalsIgnoreCase(settings)) {
                // Load the preferences from an XML resource
                preferenceFile_toLoad= R.xml.preferences_general;
            }else if ("prefs_location".equalsIgnoreCase(settings)) {
                // Load the preferences from an XML resource
                preferenceFile_toLoad=R.xml.preferences_location;
            }else if ("prefs_athan".equals(settings)) {
                // Load the preferences from an XML resource
                preferenceFile_toLoad=R.xml.preferences_athan;
            }

            addPreferencesFromResource(preferenceFile_toLoad);

        }

    }


}
