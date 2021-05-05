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

class WidgetAgeConfigureFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_widget_age, rootKey)
    }

    private val appWidgetId: Int?
        get() = arguments
            ?.takeIf { it.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)

    override fun onPreferenceTreeClick(preference: Preference?) = appWidgetId?.let { appWidgetId ->
        when (preference?.key) {
            PREF_SELECTED_WIDGET_TEXT_COLOR ->
                showColorPickerDialog(isBackgroundPick = false, key = preference.key + appWidgetId)
            PREF_SELECTED_WIDGET_BACKGROUND_COLOR ->
                showColorPickerDialog(isBackgroundPick = true, key = preference.key + appWidgetId)
            PREF_SELECTED_DATE_AGE_WIDGET ->
                showDayPickerDialog(preference.key)
            else -> null
        }
    } ?: super.onPreferenceTreeClick(preference)
}
