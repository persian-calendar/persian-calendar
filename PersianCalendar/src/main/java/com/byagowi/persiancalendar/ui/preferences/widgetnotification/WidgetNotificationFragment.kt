package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.ui.preferences.shared.showColorPickerDialog
import com.byagowi.persiancalendar.utils.appPrefs

// Don't use MainActivity here as it is used in WidgetConfigurationActivity also
class WidgetNotificationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        addPreferencesFromResource(R.xml.preferences_widget_notification)

    override fun onPreferenceTreeClick(preference: Preference?): Boolean = when (
        val key = preference?.key
    ) {
        PREF_SELECTED_WIDGET_TEXT_COLOR -> {
            showColorPickerDialog(
                isBackgroundPick = false, initialColor = activity?.appPrefs
                    ?.getString(key, null) ?: DEFAULT_SELECTED_WIDGET_TEXT_COLOR
            ) { activity?.appPrefs?.edit { putString(key, it) } }
            true
        }
        PREF_SELECTED_WIDGET_BACKGROUND_COLOR -> {
            showColorPickerDialog(
                isBackgroundPick = true, initialColor = activity?.appPrefs
                    ?.getString(key, null) ?: DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
            ) { activity?.appPrefs?.edit { putString(key, it) } }
            true
        }
        else -> super.onPreferenceTreeClick(preference)
    }
}
