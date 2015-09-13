package com.byagowi.persiancalendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.io.IOException;

/**
 * Preference activity
 *
 * @author ebraminio
 */
public class ApplicationPreference extends PreferenceActivity {
    private final Utils utils = Utils.getInstance();

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

            loadFonts(getActivity(), (ListPreference) findPreference("CalendarFont"));
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
}
