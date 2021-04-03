package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.CalendarPreferenceDialog
import com.byagowi.persiancalendar.utils.askForCalendarPermission
import com.byagowi.persiancalendar.utils.language

class InterfaceCalendarFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_interface_calendar)

        findPreference<ListPreference>("Theme")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()
        findPreference<ListPreference>("AppLanguage")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()
        if (language != LANG_AR)
            findPreference<SwitchPreferenceCompat>(PREF_EASTERN_GREGORIAN_ARABIC_MONTHS)
                ?.layoutResource = R.layout.empty
        findPreference<ListPreference>("WeekStart")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()
        when (language) {
            LANG_EN_US, LANG_JA -> findPreference<ListPreference>(PREF_PERSIAN_DIGITS)
                ?.layoutResource = R.layout.empty
        }


        val switchPreference = findPreference<SwitchPreferenceCompat>("showDeviceCalendarEvents")

        val activity = activity ?: return
        switchPreference?.setOnPreferenceChangeListener { _, _ ->
            if (ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askForCalendarPermission(activity)
                switchPreference.isChecked = false
            } else {
                switchPreference.isChecked = !switchPreference.isChecked
            }
            false
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean =
        if (preference?.key == "calendars_priority") {
            parentFragmentManager.apply {
                CalendarPreferenceDialog().show(this, "CalendarPreferenceDialog")
            }
            true
        } else super.onPreferenceTreeClick(preference)
}
