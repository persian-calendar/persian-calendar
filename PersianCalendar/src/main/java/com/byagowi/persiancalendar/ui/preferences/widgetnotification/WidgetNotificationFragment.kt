package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.preferences.shared.showColorPickerDialog

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
class WidgetNotificationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        addPreferencesFromResource(R.xml.preferences_widget_notification)

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val key = preference?.key ?: return super.onPreferenceTreeClick(preference)
        return when (key) {
            PREF_SELECTED_WIDGET_TEXT_COLOR -> showColorPickerDialog(false, key)
            PREF_SELECTED_WIDGET_BACKGROUND_COLOR -> showColorPickerDialog(true, key)
            else -> super.onPreferenceTreeClick(preference)
        }
    }
}
