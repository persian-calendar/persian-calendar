package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.byagowi.persiancalendar.utils.setOnClickListener

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
class WidgetNotificationFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = context ?: return

        val screen = preferenceManager.createPreferenceScreen(context)
        val isWidgetsConfiguration = arguments?.getBoolean(IS_WIDGETS_CONFIGURATION, false) == true
        val handler = Handler(Looper.getMainLooper())
        listOf(
            SwitchPreferenceCompat(context).also {
                it.key = PREF_NOTIFY_DATE
                it.setDefaultValue(true)
                it.setTitle(R.string.notify_date)
                it.setSummary(R.string.enable_notify)
                if (isWidgetsConfiguration) it.isVisible = false
            },
            SwitchPreferenceCompat(context).also {
                it.key = PREF_NOTIFY_DATE_LOCK_SCREEN
                handler.post { it.dependency = PREF_NOTIFY_DATE } // deferred dependency wire up
                it.setDefaultValue(true)
                it.setTitle(R.string.notify_date_lock_screen)
                it.setSummary(R.string.notify_date_lock_screen_summary)
                if (isWidgetsConfiguration) it.isVisible = false
            },
            Preference(context).also {
                it.setTitle(R.string.widget_text_color)
                it.setSummary(R.string.select_widgets_text_color)
                it.setOnClickListener {
                    showColorPickerDialog(false, PREF_SELECTED_WIDGET_TEXT_COLOR)
                }
            },
            Preference(context).also {
                it.setTitle(R.string.widget_background_color)
                it.setSummary(R.string.select_widgets_background_color)
                it.setOnClickListener {
                    showColorPickerDialog(true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR)
                }
            },
            SwitchPreferenceCompat(context).also {
                it.key = PREF_NUMERICAL_DATE_PREFERRED
                it.setDefaultValue(false)
                it.setTitle(R.string.prefer_linear_date)
                it.setSummary(R.string.prefer_linear_date_summary)
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
                it.setDefaultValue(resources.getStringArray(R.array.what_to_show_default).toSet())
                it.entries = resources.getStringArray(R.array.what_to_show)
                it.entryValues = resources.getStringArray(R.array.what_to_show_keys)
            }
        ).onEach { it.isIconSpaceReserved = false }.forEach(screen::addPreference)
        preferenceScreen = screen
    }

    companion object {
        const val IS_WIDGETS_CONFIGURATION = "IS_WIDGETS_CONFIGURATION"
    }
}
