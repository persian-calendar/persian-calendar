package com.byagowi.persiancalendar.view.preferences;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.view.dialog.preferredcalendars.CalendarPreferenceDialog;

import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class FragmentInterfaceCalendar extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences_interface_calendar);

        SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) findPreference("showDeviceCalendarEvents");

        switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                UIUtils.askForCalendarPermission(getActivity());
                switchPreference.setChecked(false);
            } else {
                if (switchPreference.isChecked()) {
                    switchPreference.setChecked(false);
                } else {
                    switchPreference.setChecked(true);
                }
            }
            return false;
        });
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("calendars_priority")) {
            new CalendarPreferenceDialog().show(getFragmentManager(), "CalendarPreferenceDialog");
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }
}
