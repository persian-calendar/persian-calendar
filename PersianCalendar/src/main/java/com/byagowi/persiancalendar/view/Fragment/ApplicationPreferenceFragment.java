package com.byagowi.persiancalendar.view.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.custom.AthanVolumeDialog;
import com.byagowi.persiancalendar.view.custom.AthanVolumePreference;
import com.byagowi.persiancalendar.view.custom.PrayerSelectDialog;
import com.byagowi.persiancalendar.view.custom.PrayerSelectPreference;

/**
 * Preference activity
 *
 * @author ebraminio
 */
public class ApplicationPreferenceFragment extends PreferenceFragmentCompat {
    //    private final Utils utils = Utils.getInstance();
    private static SharedPreferences prefs;
    private static Preference categoryAthan;
    private static Preference prefLocation;
    private static Preference prefLatitude;
    private static Preference prefLongitude;

    private static String locationName;
    private static double latitude;
    private static double longitude;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        locationName = prefs.getString("Location", "");
        String strLat = prefs.getString("Latitude", "0");
        String strLng = prefs.getString("Longitude", "0");
        latitude = TextUtils.isEmpty(strLat) ? 0 : Double.parseDouble(strLat);
        longitude = TextUtils.isEmpty(strLng) ? 0 : Double.parseDouble(strLng);

        addPreferencesFromResource(R.xml.preferences);

        LocationPreferencesChangeListener prefChangeListener = new LocationPreferencesChangeListener();
        categoryAthan = findPreference("Athan");
        prefLocation = findPreference("Location");
        prefLatitude = findPreference("Latitude");
        prefLongitude = findPreference("Longitude");
        prefLocation.setOnPreferenceChangeListener(prefChangeListener);
        prefLatitude.setOnPreferenceChangeListener(prefChangeListener);
        prefLongitude.setOnPreferenceChangeListener(prefChangeListener);

        updateAthanPreferencesState(null, null);
    }

    public static void updateAthanPreferencesState(Preference pref, Object newValue) {
        if (pref != null && newValue != null) {
            String strNewValue = String.valueOf(newValue);
            if (pref.getKey().equals("Location")) {
                locationName = strNewValue;
            } else if (pref.getKey().equals("Latitude")) {
                latitude = TextUtils.isEmpty(strNewValue) ? 0 : Double.parseDouble(strNewValue);
            } else if (pref.getKey().equals("Longitude")) {
                longitude = TextUtils.isEmpty(strNewValue) ? 0 : Double.parseDouble(strNewValue);
            }
        }

        boolean locationEmpty = (TextUtils.isEmpty(locationName) || locationName.equalsIgnoreCase("CUSTOM")) && (latitude == 0 || longitude == 0);
        categoryAthan.setEnabled(!locationEmpty);
        if (locationEmpty) {
            categoryAthan.setSummary(R.string.athan_disabled_summary);
        } else {
            categoryAthan.setSummary("");
        }
    }

    private static class LocationPreferencesChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            updateAthanPreferencesState(preference, newValue);
            return true;
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment;
        if (preference instanceof PrayerSelectPreference) {
            fragment = PrayerSelectDialog.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else if (preference instanceof AthanVolumePreference) {
            fragment = AthanVolumeDialog.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
