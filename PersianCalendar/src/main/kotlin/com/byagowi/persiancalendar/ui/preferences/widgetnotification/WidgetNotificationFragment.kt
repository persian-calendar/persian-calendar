package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.DEFAULT_WIDGET_CUSTOMIZATIONS
import com.byagowi.persiancalendar.NON_HOLIDAYS_EVENTS_KEY
import com.byagowi.persiancalendar.OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.OWGHAT_KEY
import com.byagowi.persiancalendar.OWGHAT_LOCATION_KEY
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
import com.byagowi.persiancalendar.ui.preferences.build
import com.byagowi.persiancalendar.ui.preferences.clickable
import com.byagowi.persiancalendar.ui.preferences.dialogTitle
import com.byagowi.persiancalendar.ui.preferences.multiSelect
import com.byagowi.persiancalendar.ui.preferences.section
import com.byagowi.persiancalendar.ui.preferences.shared.showColorPickerDialog
import com.byagowi.persiancalendar.ui.preferences.summary
import com.byagowi.persiancalendar.ui.preferences.switch
import com.byagowi.persiancalendar.ui.preferences.title
import com.byagowi.persiancalendar.utils.language

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
class WidgetNotificationFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val handler = Handler(Looper.getMainLooper())
        val activity = activity ?: return
        preferenceScreen = preferenceManager.createPreferenceScreen(context).build {
            section(R.string.pref_notification) {
                // Hide notification category if we are in widgets configuration
                if (arguments?.getBoolean(IS_WIDGETS_CONFIGURATION, false) == true)
                    isVisible = false
                switch(PREF_NOTIFY_DATE, true) {
                    title(R.string.notify_date)
                    summary(R.string.enable_notify)
                }
                switch(PREF_NOTIFY_DATE_LOCK_SCREEN, true) {
                    title(R.string.notify_date_lock_screen)
                    summary(R.string.notify_date_lock_screen_summary)
                    handler.post { dependency = PREF_NOTIFY_DATE } // deferred dependency wire up
                }
            }
            section(R.string.pref_widget) {
                // Mark the rest of options as advanced
                initialExpandedChildrenCount = 5
                clickable(onClick = {
                    showColorPickerDialog(activity, false, PREF_SELECTED_WIDGET_TEXT_COLOR)
                }) {
                    title(R.string.widget_text_color)
                    summary(R.string.select_widgets_text_color)
                }
                clickable(onClick = {
                    showColorPickerDialog(activity, true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR)
                }) {
                    title(R.string.widget_background_color)
                    summary(R.string.select_widgets_background_color)
                }
                switch(PREF_NUMERICAL_DATE_PREFERRED, false) {
                    title(R.string.prefer_linear_date)
                    summary(R.string.prefer_linear_date_summary)
                }
                switch(PREF_WIDGET_CLOCK, true) {
                    title(R.string.clock_on_widget)
                    summary(R.string.showing_clock_on_widget)
                }
                switch(PREF_CENTER_ALIGN_WIDGETS, false) {
                    title(R.string.center_align_widgets)
                    summary(R.string.center_align_widgets_summary)
                }
                switch(PREF_IRAN_TIME, false) {
                    title(R.string.iran_time)
                    summary(R.string.showing_iran_time)
                    isVisible = language.isIranExclusive
                }
                val widgetCustomizations = listOf(
                    OTHER_CALENDARS_KEY to R.string.widget_customization_other_calendars,
                    NON_HOLIDAYS_EVENTS_KEY to R.string.widget_customization_non_holiday_events,
                    OWGHAT_KEY to R.string.widget_customization_owghat,
                    OWGHAT_LOCATION_KEY to R.string.widget_customization_owghat_location
                )
                multiSelect(
                    PREF_WHAT_TO_SHOW_WIDGETS,
                    widgetCustomizations.map { (_, title) -> getString(title) },
                    widgetCustomizations.map { (key, _) -> key },
                    DEFAULT_WIDGET_CUSTOMIZATIONS
                ) {
                    title(R.string.customize_widget)
                    summary(R.string.customize_widget_summary)
                    dialogTitle(R.string.which_one_to_show)
                }
            }
        }
    }

    companion object {
        const val IS_WIDGETS_CONFIGURATION = "IS_WIDGETS_CONFIGURATION"
    }
}
