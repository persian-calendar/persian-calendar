package com.byagowi.persiancalendar.ui.preferences.widgetnotification

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.utils.appPrefs
import java.util.*

// Don't use MainActivity here as it is used in WidgetConfigurationActivity also
class WidgetNotificationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
            addPreferencesFromResource(R.xml.preferences_widget_notification)

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val activity = activity ?: return false

        val sharedPreferences = activity.appPrefs

        if (preference?.key == PREF_SELECTED_WIDGET_TEXT_COLOR) {
            val colorPickerView = ColorPickerView(activity)
            colorPickerView.setColorsToPick(
                    listOf(0xFFFFFFFFL, 0xFFE65100L, 0xFF00796bL, 0xFFFEF200L, 0xFF202020L)
            )
            colorPickerView.setPickedColor(
                    Color.parseColor(
                            sharedPreferences.getString(
                                    PREF_SELECTED_WIDGET_TEXT_COLOR,
                                    DEFAULT_SELECTED_WIDGET_TEXT_COLOR
                            )
                    )
            )
            colorPickerView.hideAlphaSeekBar()

            val padding = (activity.resources.displayMetrics.density * 10).toInt()
            colorPickerView.setPadding(padding, padding, padding, padding)

            AlertDialog.Builder(activity).apply {
                setTitle(R.string.widget_text_color)
                setView(colorPickerView)
                setPositiveButton(R.string.accept) { _, _ ->
                    try {
                        sharedPreferences.edit {
                            putString(
                                    PREF_SELECTED_WIDGET_TEXT_COLOR,
                                    "#%06X".format(
                                            Locale.ENGLISH,
                                            0xFFFFFF and colorPickerView.pickerColor
                                    )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                setNegativeButton(R.string.cancel, null)
            }.show()
            return true
        }

        if (preference?.key == PREF_SELECTED_WIDGET_BACKGROUND_COLOR) {
            val colorPickerView = ColorPickerView(activity)
            colorPickerView.setColorsToPick(listOf(0x00000000L, 0x50000000L, 0xFF000000L))
            colorPickerView.setPickedColor(
                    Color.parseColor(
                            sharedPreferences.getString(
                                    PREF_SELECTED_WIDGET_BACKGROUND_COLOR,
                                    DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
                            )
                    )
            )

            val padding = (activity.resources.displayMetrics.density * 10).toInt()
            colorPickerView.setPadding(padding, padding, padding, padding)

            AlertDialog.Builder(activity).apply {
                setTitle(R.string.widget_background_color)
                setView(colorPickerView)
                setPositiveButton(R.string.accept) { _, _ ->
                    try {
                        sharedPreferences.edit {
                            putString(
                                    PREF_SELECTED_WIDGET_BACKGROUND_COLOR,
                                    "#%08X".format(
                                            Locale.ENGLISH,
                                            0xFFFFFFFF and colorPickerView.pickerColor.toLong()
                                    )
                            )
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
