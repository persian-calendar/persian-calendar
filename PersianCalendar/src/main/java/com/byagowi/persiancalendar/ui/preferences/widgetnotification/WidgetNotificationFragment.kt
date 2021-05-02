package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.ui.preferences.shared.showColorPickerDialog

// Consider that it is used both in MainActivity and WidgetConfigurationActivity
class WidgetNotificationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        addPreferencesFromResource(R.xml.preferences_widget_notification)

    override fun onPreferenceTreeClick(preference: Preference?): Boolean = when (
        val key = preference?.key
    ) {
        PREF_SELECTED_WIDGET_TEXT_COLOR -> showColorPickerDialog(isBackgroundPick = false, key)
        PREF_SELECTED_WIDGET_BACKGROUND_COLOR -> showColorPickerDialog(isBackgroundPick = true, key)
        else -> super.onPreferenceTreeClick(preference)
    }
}
