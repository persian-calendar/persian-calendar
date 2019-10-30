package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.CalendarPreferenceDialog
import com.byagowi.persiancalendar.utils.askForCalendarPermission
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FragmentInterfaceCalendar : PreferenceFragmentCompat() {

    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_interface_calendar)

        findPreference<ListPreference>("AppLanguage")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()
        findPreference<ListPreference>("Theme")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()
        findPreference<ListPreference>("WeekStart")?.summaryProvider =
            ListPreference.SimpleSummaryProvider.getInstance()

        val switchPreference = findPreference<SwitchPreferenceCompat>("showDeviceCalendarEvents")

        switchPreference?.setOnPreferenceChangeListener { _, _ ->
            if (ActivityCompat.checkSelfPermission(
                    mainActivityDependency.mainActivity,
                    Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askForCalendarPermission(mainActivityDependency.mainActivity)
                switchPreference.isChecked = false
            } else {
                switchPreference.isChecked = !switchPreference.isChecked
            }
            false
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == "calendars_priority") {
            val fragmentManager = fragmentManager
            if (fragmentManager != null) {
                CalendarPreferenceDialog().show(fragmentManager, "CalendarPreferenceDialog")
            }
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }
}
