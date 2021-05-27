package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.LANG_AR
import com.byagowi.persiancalendar.LANG_EN_US
import com.byagowi.persiancalendar.LANG_JA
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_PERSIAN_DIGITS
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.showCalendarPreferenceDialog
import com.byagowi.persiancalendar.utils.askForCalendarPermission
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.returnTrue

class InterfaceCalendarFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_interface_calendar)

        val summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        findPreference<ListPreference>(PREF_THEME)?.summaryProvider = summaryProvider
        findPreference<ListPreference>(PREF_APP_LANGUAGE)?.summaryProvider = summaryProvider
        if (language != LANG_AR)
            findPreference<SwitchPreferenceCompat>(PREF_EASTERN_GREGORIAN_ARABIC_MONTHS)
                ?.isVisible = false
        findPreference<ListPreference>(PREF_WEEK_START)?.summaryProvider = summaryProvider
        when (language) {
            LANG_EN_US, LANG_JA -> findPreference<SwitchPreferenceCompat>(PREF_PERSIAN_DIGITS)
                ?.isVisible = false
        }

        val showDeviceCalendarSwitch = findPreference<SwitchPreferenceCompat>(
            PREF_SHOW_DEVICE_CALENDAR_EVENTS
        )
        showDeviceCalendarSwitch?.setOnPreferenceChangeListener { _, _ ->
            activity?.let { activity ->
                if (ActivityCompat.checkSelfPermission(
                        activity, Manifest.permission.READ_CALENDAR
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    askForCalendarPermission(activity)
                    showDeviceCalendarSwitch.isChecked = false
                } else {
                    showDeviceCalendarSwitch.isChecked = !showDeviceCalendarSwitch.isChecked
                }
            }
            false
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean =
        if (preference?.key == "calendars_priority") showCalendarPreferenceDialog().let { true }
        else super.onPreferenceTreeClick(preference)
}
