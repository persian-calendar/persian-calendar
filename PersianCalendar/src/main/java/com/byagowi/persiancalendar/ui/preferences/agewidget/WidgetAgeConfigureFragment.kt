package com.byagowi.persiancalendar.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.ui.calendar.dialogs.showSelectDayDialog
import com.byagowi.persiancalendar.ui.preferences.shared.showColorPickerDialog
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getTodayJdn

class WidgetAgeConfigureFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_widget_age, rootKey)
    }

    private val appWidgetId: Int?
        get() = arguments
            ?.takeIf { it.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)

    override fun onPreferenceTreeClick(preference: Preference?) = appWidgetId?.let { appWidgetId ->
        val key = (preference?.key ?: return@let null) + appWidgetId
        when (preference.key) {
            PREF_SELECTED_WIDGET_TEXT_COLOR -> {
                showColorPickerDialog(
                    isBackgroundPick = false, initialColor = activity?.appPrefs
                        ?.getString(key, null) ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR
                ) { colorResult -> activity?.appPrefs?.edit { putString(key, colorResult) } }
                true
            }
            PREF_SELECTED_WIDGET_BACKGROUND_COLOR -> {
                showColorPickerDialog(
                    isBackgroundPick = true, initialColor = activity?.appPrefs
                        ?.getString(key, null) ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
                ) { colorResult -> activity?.appPrefs?.edit { putString(key, colorResult) } }
                true
            }
            PREF_SELECTED_DATE_AGE_WIDGET -> {
                val todayJdn = getTodayJdn()
                showSelectDayDialog(
                    jdn = activity?.appPrefs?.getLong(key, todayJdn) ?: todayJdn
                ) { jdnResult -> activity?.appPrefs?.edit { putLong(key, jdnResult) } }
                true
            }
            else -> null
        }
    } ?: super.onPreferenceTreeClick(preference)
}