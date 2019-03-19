package com.byagowi.persiancalendar.ui.preferences.interfacecalendar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.CalendarPreferenceDialog;
import com.byagowi.persiancalendar.utils.Utils;

import javax.inject.Inject;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import dagger.android.support.AndroidSupportInjection;

public class FragmentInterfaceCalendar extends PreferenceFragmentCompat {
    @Inject
    MainActivityDependency mainActivityDependency;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        AndroidSupportInjection.inject(this);

        addPreferencesFromResource(R.xml.preferences_interface_calendar);

        SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) findPreference("showDeviceCalendarEvents");

        switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {

            if (ActivityCompat.checkSelfPermission(mainActivityDependency.getMainActivity(), Manifest.permission.READ_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                Utils.askForCalendarPermission(mainActivityDependency.getMainActivity());
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
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                new CalendarPreferenceDialog().show(fragmentManager, "CalendarPreferenceDialog");
            }
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }
}
