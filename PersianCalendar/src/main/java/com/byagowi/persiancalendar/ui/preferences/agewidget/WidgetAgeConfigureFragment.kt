package com.byagowi.persiancalendar.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.preferences.shared.showColorPickerDialog
import com.byagowi.persiancalendar.utils.setOnClickListener

class WidgetAgeConfigureFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = context ?: return
        val appWidgetId = arguments
            ?.takeIf { it.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) ?: return

        val screen = preferenceManager.createPreferenceScreen(context)
        listOf(
            Preference(context).also {
                it.setTitle(R.string.select_date)
                it.setOnClickListener { showDayPickerDialog(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId) }
            },
            Preference(context).also {
                it.setTitle(R.string.widget_text_color)
                it.setSummary(R.string.select_widgets_text_color)
                it.setOnClickListener {
                    showColorPickerDialog(false, PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId)
                }
            },
            Preference(context).also {
                it.setTitle(R.string.widget_background_color)
                it.setSummary(R.string.select_widgets_background_color)
                it.setOnClickListener {
                    showColorPickerDialog(true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId)
                }
            }
        ).onEach { it.isIconSpaceReserved = false }.forEach(screen::addPreference)
        preferenceScreen = screen
    }
}
