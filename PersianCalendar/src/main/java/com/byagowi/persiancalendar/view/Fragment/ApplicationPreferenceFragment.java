package com.byagowi.persiancalendar.view.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import com.byagowi.persiancalendar.R;

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
//        utils.setTheme(getContext());
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
        loadFonts(getContext().getApplicationContext(), (ListPreference) findPreference("CalendarFont"));
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

    // this reads the font list in assets/fonts/ and puts them in the ListPreference
    public static void loadFonts(Context context, ListPreference listPreference) {
//        CharSequence[] fontList = new CharSequence[0];
//        try {
//            fontList = context.getAssets().list("fonts");
//        } catch (IOException e) {
//            Log.e("ApplicationPreferenceFragment", "", e);
//        }
//
//        listPreference.setEntries(fontList);
//        listPreference.setEntryValues(fontList);
//        listPreference.setDefaultValue("NotoNaskhArabic-Regular.ttf");
    }

    private static class LocationPreferencesChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            updateAthanPreferencesState(preference, newValue);
            return true;
        }
    }
}
