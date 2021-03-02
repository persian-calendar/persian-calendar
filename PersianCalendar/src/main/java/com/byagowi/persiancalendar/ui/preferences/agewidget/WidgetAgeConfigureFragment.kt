package com.byagowi.persiancalendar.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.ui.calendar.dialogs.SelectDayDialog
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.ColorPickerView
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.getTodayJdn
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class WidgetAgeConfigureFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_widget_age, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val activity = activity ?: return false

        arguments?.let {
            if (!it.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
                return false
        }
        val appWidgetId = arguments?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)

        val sharedPreferences = activity.appPrefs

        if (preference?.key == PREF_SELECTED_WIDGET_TEXT_COLOR) {
            val colorPickerView = ColorPickerView(activity)
            colorPickerView.setColorsToPick(
                listOf(0xFFFFFFFFL, 0xFFE65100L, 0xFF00796bL, 0xFFFEF200L, 0xFF202020L)
            )
            colorPickerView.setPickedColor(
                Color.parseColor(
                    sharedPreferences.getString(
                        PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId,
                        DEFAULT_SELECTED_WIDGET_TEXT_COLOR
                    )
                )
            )
            colorPickerView.hideAlphaSeekBar()

            val padding = 10.dp
            colorPickerView.setPadding(padding, padding, padding, padding)

            MaterialAlertDialogBuilder(activity).apply {
                setTitle(R.string.widget_text_color)
                setView(colorPickerView)
                setPositiveButton(R.string.accept) { _, _ ->
                    try {
                        sharedPreferences.edit {
                            putString(
                                PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId,
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
                        PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId,
                        DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
                    )
                )
            )

            val padding = 10.dp
            colorPickerView.setPadding(padding, padding, padding, padding)

            MaterialAlertDialogBuilder(activity).apply {
                setTitle(R.string.widget_background_color)
                setView(colorPickerView)
                setPositiveButton(R.string.accept) { _, _ ->
                    try {
                        sharedPreferences.edit {
                            putString(
                                PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId,
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

        if (preference?.key == PREF_SELECTED_DATE_AGE_WIDGET) {
            SelectDayDialog.newInstance(
                sharedPreferences.getLong(
                    PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId,
                    getTodayJdn()
                )
            ).apply {
                onSuccess = fun(jdn: Long) {
                    sharedPreferences.edit {
                        putLong(
                            PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId,
                            jdn
                        )
                    }
                }
            }.show(childFragmentManager, SelectDayDialog::class.java.name)
        }
        return super.onPreferenceTreeClick(preference)
    }
}