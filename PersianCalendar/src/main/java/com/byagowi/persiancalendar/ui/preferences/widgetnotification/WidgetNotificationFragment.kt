package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.os.Bundle
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.byagowi.persiancalendar.PREF_CENTER_ALIGN_WIDGETS
import com.byagowi.persiancalendar.PREF_IRAN_TIME
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE_LOCK_SCREEN
import com.byagowi.persiancalendar.PREF_NUMERICAL_DATE_PREFERRED
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_WHAT_TO_SHOW_WIDGETS
import com.byagowi.persiancalendar.PREF_WIDGET_CLOCK
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.preferences.shared.showColorPickerDialog

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
class WidgetNotificationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = context ?: return

        val screen = preferenceManager.createPreferenceScreen(context)
        listOf(
            Preference(context).also {
                it.setTitle(R.string.widget_text_color)
                it.setSummary(R.string.select_widgets_text_color)
                it.isIconSpaceReserved = false
                it.setOnPreferenceClickListener {
                    showColorPickerDialog(false, PREF_SELECTED_WIDGET_TEXT_COLOR)
                    true
                }
            },
            Preference(context).also {
                it.setTitle(R.string.widget_background_color)
                it.setSummary(R.string.select_widgets_background_color)
                it.isIconSpaceReserved = false
                it.setOnPreferenceClickListener {
                    showColorPickerDialog(true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR)
                    true
                }
            },
            SwitchPreferenceCompat(context).also {
                it.key = PREF_NUMERICAL_DATE_PREFERRED
                it.setDefaultValue(false)
                it.setTitle(R.string.prefer_linear_date)
                it.setSummary(R.string.prefer_linear_date_summary)
                it.isIconSpaceReserved = false
            },
            SwitchPreferenceCompat(context).also {
                it.key = PREF_WIDGET_CLOCK
                it.setDefaultValue(true)
                it.setTitle(R.string.clock_on_widget)
                it.setSummary(R.string.showing_clock_on_widget)
            },
            SwitchPreferenceCompat(context).also {
                it.key = PREF_CENTER_ALIGN_WIDGETS
                it.setDefaultValue(false)
                it.setTitle(R.string.center_align_widgets)
                it.setSummary(R.string.center_align_widgets_summary)
            },
            SwitchPreferenceCompat(context).also {
                it.key = PREF_IRAN_TIME
                it.setDefaultValue(false)
                it.setTitle(R.string.iran_time)
                it.setSummary(R.string.showing_iran_time)
            },
            MultiSelectListPreference(context).also {
                it.key = PREF_WHAT_TO_SHOW_WIDGETS
                it.setTitle(R.string.customize_widget)
                it.setSummary(R.string.customize_widget_summary)
                it.setDialogTitle(R.string.which_one_to_show)
                it.setNegativeButtonText(R.string.cancel)
                it.setPositiveButtonText(R.string.accept)
                it.setDefaultValue(R.array.what_to_show_default)
                it.setEntries(R.array.what_to_show)
                it.setEntryValues(R.array.what_to_show_keys)
            },
            SwitchPreferenceCompat(context).also {
                it.key = PREF_NOTIFY_DATE
                it.setDefaultValue(true)
                it.setTitle(R.string.notify_date)
                it.setSummary(R.string.enable_notify)
            },
            SwitchPreferenceCompat(context).also {
                it.key = PREF_NOTIFY_DATE_LOCK_SCREEN
                it.setDefaultValue(true)
                it.setTitle(R.string.notify_date_lock_screen)
                it.setSummary(R.string.notify_date_lock_screen_summary)
            }
        ).onEach { it.isIconSpaceReserved = false }.forEach(screen::addPreference)
        preferenceScreen = screen

        // wire up the dependency
        findPreference<SwitchPreferenceCompat>(PREF_NOTIFY_DATE_LOCK_SCREEN)
            ?.dependency = PREF_NOTIFY_DATE
    }
}
