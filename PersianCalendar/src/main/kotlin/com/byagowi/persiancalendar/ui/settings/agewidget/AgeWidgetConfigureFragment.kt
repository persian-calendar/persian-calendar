package com.byagowi.persiancalendar.ui.settings.agewidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.PREF_WIDGETS_PREFER_SYSTEM_COLORS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.settings.build
import com.byagowi.persiancalendar.ui.settings.clickable
import com.byagowi.persiancalendar.ui.settings.common.showColorPickerDialog
import com.byagowi.persiancalendar.ui.settings.section
import com.byagowi.persiancalendar.ui.settings.summary
import com.byagowi.persiancalendar.ui.settings.title
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.putJdn

class AgeWidgetConfigureFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val appWidgetId = arguments
            ?.takeIf { it.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) ?: return

        val activity = activity ?: return
        preferenceScreen = preferenceManager.createPreferenceScreen(activity).build {
            section(R.string.empty) {
                clickable(onClick = {
                    val key = PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId
                    val jdn = activity.appPrefs.getJdnOrNull(key) ?: Jdn.today()
                    showDayPickerDialog(activity, jdn, R.string.accept) { result ->
                        activity.appPrefs.edit { putJdn(key, result) }
                    }
                }) { title(R.string.select_date) }
                val prefs = activity.appPrefs
                val showColorOptions = !(Theme.isDynamicColor(prefs) &&
                        prefs.getBoolean(PREF_WIDGETS_PREFER_SYSTEM_COLORS, true))
                clickable(onClick = {
                    showColorPickerDialog(
                        activity, false, PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId
                    )
                }) {
                    title(R.string.widget_text_color)
                    summary(R.string.select_widgets_text_color)
                    isVisible = showColorOptions
                }
                clickable(onClick = {
                    showColorPickerDialog(
                        activity, true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId
                    )
                }) {
                    title(R.string.widget_background_color)
                    summary(R.string.select_widgets_background_color)
                    isVisible = showColorOptions
                }
            }
        }
    }
}
