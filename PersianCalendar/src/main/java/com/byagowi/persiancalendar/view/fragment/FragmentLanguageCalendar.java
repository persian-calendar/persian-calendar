package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.view.dialog.preferredcalendars.CalendarPreferenceDialog;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Preference activity
 *
 * @author ebraminio
 */
public class FragmentLanguageCalendar extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        UIUtils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.settings), "");

        addPreferencesFromResource(R.xml.preferences_language_calendar);

    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment = null;
        if (preference.getKey().equals("calendars_priority")) {
            fragment = new CalendarPreferenceDialog();
        } else {
            super.onDisplayPreferenceDialog(preference);
        }

        if (fragment != null) {
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            fragment.setArguments(bundle);
            fragment.setTargetFragment(this, 0);
            try {
                fragment.show(getFragmentManager(), fragment.getClass().getName());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
