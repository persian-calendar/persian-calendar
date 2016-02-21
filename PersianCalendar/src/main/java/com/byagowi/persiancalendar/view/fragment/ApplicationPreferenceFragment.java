package com.byagowi.persiancalendar.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.dialog.AthanVolumeDialog;
import com.byagowi.persiancalendar.view.dialog.AthanVolumePreference;
import com.byagowi.persiancalendar.view.dialog.AthanNumericDialog;
import com.byagowi.persiancalendar.view.dialog.AthanNumericPreference;
import com.byagowi.persiancalendar.view.dialog.LocationPreference;
import com.byagowi.persiancalendar.view.dialog.LocationPreferenceDialog;
import com.byagowi.persiancalendar.view.dialog.PrayerSelectDialog;
import com.byagowi.persiancalendar.view.dialog.PrayerSelectPreference;

/**
 * Preference activity
 *
 * @author ebraminio
 */
public class ApplicationPreferenceFragment extends PreferenceFragmentCompat {
    public static final String INTENT_ACTION_PREFERENCES_CHANGED = "com.byagowi.persiancalendar.intent.action.PREFERENCES_CHANGED";

    public static final String PREF_KEY_ATHAN = "Athan";
    public static final String PREF_KEY_LOCATION = "Location";
    public static final String PREF_KEY_LATITUDE = "Latitude";
    public static final String PREF_KEY_LONGITUDE = "Longitude";

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
        Utils utils = Utils.getInstance(getContext());
        utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.settings), "");

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        locationName = prefs.getString(PREF_KEY_LOCATION, "");
        String strLat = prefs.getString(PREF_KEY_LATITUDE, "0");
        String strLng = prefs.getString(PREF_KEY_LONGITUDE, "0");
        latitude = TextUtils.isEmpty(strLat) ? 0 : Double.parseDouble(strLat);
        longitude = TextUtils.isEmpty(strLng) ? 0 : Double.parseDouble(strLng);

        addPreferencesFromResource(R.xml.preferences);

        LocationPreferencesChangeListener prefChangeListener = new LocationPreferencesChangeListener();
        categoryAthan = findPreference(PREF_KEY_ATHAN);
        prefLocation = findPreference(PREF_KEY_LOCATION);
        prefLatitude = findPreference(PREF_KEY_LATITUDE);
        prefLongitude = findPreference(PREF_KEY_LONGITUDE);
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
        } else if (preference instanceof LocationPreference) {
            fragment = LocationPreferenceDialog.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    new PreferenceChangeListener(),
                    new IntentFilter(INTENT_ACTION_PREFERENCES_CHANGED));
        } else if (preference instanceof AthanNumericPreference) {
            fragment = AthanNumericDialog.newInstance(preference.getKey());
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    new PreferenceChangeListener(),
                    new IntentFilter(INTENT_ACTION_PREFERENCES_CHANGED));
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private static class LocationPreferencesChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            updateAthanPreferencesState(preference, newValue);
            return true;
        }
    }

    public class PreferenceChangeListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);

            if (intent.hasExtra(PREF_KEY_LOCATION)) {
                updateAthanPreferencesState(findPreference(PREF_KEY_LOCATION), intent.getStringExtra(PREF_KEY_LOCATION));
            }
        }
    }
}
