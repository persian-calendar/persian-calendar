package com.byagowi.persiancalendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Preference activity
 *
 * @author ebraminio
 */
public class ApplicationPreference extends PreferenceActivity {
    private static final String TAG = "ApplicationPreference";
    private final Utils utils = Utils.getInstance();
    private static SharedPreferences prefs;
    private static Preference categoryAthan;
    private static Preference prefLocation;
    private static Preference prefLatitude;
    private static Preference prefLongitude;

    private static String locationName;
    private static double latitude;
    private static double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        utils.setTheme(this);
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        locationName = prefs.getString("Location", "");
        String strLat = prefs.getString("Latitude", "0");
        String strLng = prefs.getString("Longitude", "0");
        latitude = TextUtils.isEmpty(strLat) ? 0 : Double.parseDouble(strLat);
        longitude = TextUtils.isEmpty(strLng) ? 0 : Double.parseDouble(strLng);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            lowerThanHC();
        } else {
            higherThanHC();
        }
    }

    @SuppressWarnings("deprecation")
    private void lowerThanHC() {
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
        loadFonts(getApplicationContext(), (ListPreference) findPreference("CalendarFont"));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void higherThanHC() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefFragment()).commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrefFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
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
            loadFonts(getActivity(), (ListPreference) findPreference("CalendarFont"));
        }
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
//            Log.e("ApplicationPreference", "", e);
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
