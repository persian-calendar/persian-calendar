package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.LANG_AR
import com.byagowi.persiancalendar.LANG_EN_US
import com.byagowi.persiancalendar.LANG_JA
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_PERSIAN_DIGITS
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SYSTEM_DEFAULT_THEME
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.showCalendarPreferenceDialog
import com.byagowi.persiancalendar.utils.askForCalendarPermission
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.setOnClickListener

class InterfaceCalendarFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = context ?: return

        val screen = preferenceManager.createPreferenceScreen(context)
        listOf(
            R.string.pref_interface to listOf(
                ListPreference(context).also {
                    it.key = PREF_APP_LANGUAGE
                    it.setTitle(R.string.language)
                    it.setDialogTitle(R.string.language)
                    it.setEntries(R.array.languageNames)
                    it.setEntryValues(R.array.languageKeys)
                    it.setNegativeButtonText(R.string.cancel)
                    it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                },
                ListPreference(context).also {
                    it.key = PREF_THEME
                    it.setTitle(R.string.select_skin)
                    it.setDialogTitle(R.string.select_skin)
                    it.setDefaultValue(SYSTEM_DEFAULT_THEME)
                    it.setEntries(R.array.themeNames)
                    it.setEntryValues(R.array.themeKeys)
                    it.setNegativeButtonText(R.string.cancel)
                    it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                },
                SwitchPreferenceCompat(context).also {
                    it.key = PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
                    it.setDefaultValue(false)
                    if (language == LANG_AR) {
                        it.title = "السنة الميلادية بالاسماء الشرقية"
                        it.summary = "كانون الثاني، شباط، آذار، …"
                    } else it.isVisible = false
                },
                SwitchPreferenceCompat(context).also {
                    it.key = PREF_PERSIAN_DIGITS
                    it.setDefaultValue(true)
                    it.setTitle(R.string.persian_digits)
                    it.setSummary(R.string.enable_persian_digits)
                    when (language) {
                        LANG_EN_US, LANG_JA -> it.isVisible = false
                    }
                }
            ),
            R.string.calendar to listOf(
                MultiSelectListPreference(context).also {
                    it.key = PREF_HOLIDAY_TYPES
                    it.setTitle(R.string.events)
                    it.setSummary(R.string.events_summary)
                    it.setDialogTitle(R.string.events)
                    it.setDefaultValue(resources.getStringArray(R.array.default_holidays).toSet())
                    it.entries = resources.getStringArray(R.array.holidays_types)
                    it.entryValues = resources.getStringArray(R.array.holidays_values)
                    it.setNegativeButtonText(R.string.cancel)
                    it.setPositiveButtonText(R.string.accept)
                },
                SwitchPreferenceCompat(context).also {
                    it.key = PREF_SHOW_DEVICE_CALENDAR_EVENTS
                    it.setDefaultValue(false)
                    it.setTitle(R.string.show_device_calendar_events)
                    it.setSummary(R.string.show_device_calendar_events_summary)
                    it.setOnPreferenceChangeListener { _, _ ->
                        val activity = activity ?: return@setOnPreferenceChangeListener false
                        if (ActivityCompat.checkSelfPermission(
                                activity, Manifest.permission.READ_CALENDAR
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            askForCalendarPermission(activity)
                            it.isChecked = false
                        } else {
                            it.isChecked = !it.isChecked
                        }
                        false
                    }
                },
                Preference(context).also {
                    it.setTitle(R.string.calendars_priority)
                    it.setSummary(R.string.calendars_priority_summary)
                    it.setOnClickListener { showCalendarPreferenceDialog() }
                },
                SwitchPreferenceCompat(context).also {
                    it.key = PREF_ASTRONOMICAL_FEATURES
                    it.setDefaultValue(false)
                    it.setTitle(R.string.astronomical_info)
                    it.setSummary(R.string.astronomical_info_summary)
                },
                SwitchPreferenceCompat(context).also {
                    it.key = PREF_SHOW_WEEK_OF_YEAR_NUMBER
                    it.setDefaultValue(false)
                    it.setTitle(R.string.week_of_year)
                    it.setSummary(R.string.week_of_year_summary)
                },
                SwitchPreferenceCompat(context).also {
                    it.key = PREF_WIDGET_IN_24
                    it.setDefaultValue(true)
                    it.setTitle(R.string.clock_in_24)
                    it.setSummary(R.string.showing_clock_in_24)
                },
                ListPreference(context).also {
                    it.key = PREF_ISLAMIC_OFFSET
                    it.setTitle(R.string.islamic_offset)
                    it.setSummary(R.string.islamic_offset_summary)
                    it.setDialogTitle(R.string.islamic_offset)
                    it.setDefaultValue("0")
                    it.setEntries(R.array.islamicOffsetNames)
                    it.setEntryValues(R.array.islamicOffsetKeys)
                    it.setNegativeButtonText(R.string.cancel)
                },
                ListPreference(context).also {
                    it.key = PREF_WEEK_START
                    it.setTitle(R.string.week_start)
                    it.setDialogTitle(R.string.week_start_summary)
                    it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                    it.setDefaultValue("0")
                    it.setEntries(R.array.week_days)
                    it.setEntryValues(R.array.week_days_value)
                    it.setNegativeButtonText(R.string.cancel)
                },
                MultiSelectListPreference(context).also {
                    it.key = PREF_WEEK_ENDS
                    it.setTitle(R.string.week_ends)
                    it.setSummary(R.string.week_ends_summary)
                    it.setDialogTitle(R.string.week_ends_summary)
                    it.setDefaultValue(resources.getStringArray(R.array.default_weekends).toSet())
                    it.entries = resources.getStringArray(R.array.week_days)
                    it.entryValues = resources.getStringArray(R.array.week_days_value)
                    it.setNegativeButtonText(R.string.cancel)
                    it.setPositiveButtonText(R.string.accept)
                }
            )
        ).forEach { (title, preferences) ->
            val category = PreferenceCategory(context)
            category.key = title.toString() // Needed for expandable categories
            category.setTitle(title)
            category.initialExpandedChildrenCount = 6 // only needed for calendar category
            category.isIconSpaceReserved = false
            screen.addPreference(category)
            preferences.onEach { it.isIconSpaceReserved = false }.forEach(category::addPreference)
        }
        preferenceScreen = screen
    }
}
