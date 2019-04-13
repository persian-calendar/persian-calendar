package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.Constants.DEFAULT_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.Constants.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import java.util.*

// Don't use dagger in this class as it is used in WidgetConfigurationActivity also
class FragmentWidgetNotification : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
            addPreferencesFromResource(R.xml.preferences_widget_notification)

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val activity = activity ?: return false

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        if (preference?.key == PREF_SELECTED_WIDGET_TEXT_COLOR) {
            val colorPickerView = ColorPickerView(activity)
            colorPickerView.setColorsToPick(
                    arrayOf(0xFFFFFFFF, 0xFFE65100, 0xFF00796b, 0xFFFEF200, 0xFF202020)
                            .map(Long::toInt).toIntArray())
            colorPickerView.setPickedColor(Color.parseColor(
                    sharedPreferences.getString(
                            PREF_SELECTED_WIDGET_TEXT_COLOR,
                            DEFAULT_SELECTED_WIDGET_TEXT_COLOR)))

            val padding = (activity.resources.displayMetrics.density * 10).toInt()
            colorPickerView.setPadding(padding, padding, padding, padding)

            AlertDialog.Builder(activity).apply {
                setTitle(R.string.widget_text_color)
                setView(colorPickerView)
                setPositiveButton(R.string.accept) { _, _ ->
                    try {
                        sharedPreferences.edit {
                            putString(PREF_SELECTED_WIDGET_TEXT_COLOR,
                                    String.format(Locale.ENGLISH, "#%06X",
                                            0xFFFFFF and colorPickerView.pickerColor))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                setNegativeButton(R.string.cancel, null)
            }.show()
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }
}
