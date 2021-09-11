package com.byagowi.persiancalendar.ui.preferences.agewidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.PREF_SELECTED_DATE_AGE_WIDGET
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_BACKGROUND_COLOR
import com.byagowi.persiancalendar.PREF_SELECTED_WIDGET_TEXT_COLOR
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.preferences.build
import com.byagowi.persiancalendar.ui.preferences.clickable
import com.byagowi.persiancalendar.ui.preferences.section
import com.byagowi.persiancalendar.ui.preferences.shared.showColorPickerDialog
import com.byagowi.persiancalendar.ui.preferences.summary
import com.byagowi.persiancalendar.ui.preferences.title
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.getJdnOrNull
import com.byagowi.persiancalendar.utils.putJdn

class AgeWidgetConfigureFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val appWidgetId = arguments
            ?.takeIf { it.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) }
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) ?: return

        val activity = activity ?: return
        preferenceScreen = preferenceManager.createPreferenceScreen(context).build {
            section(R.string.empty) {
                clickable(onClick = {
                    val key = PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId
                    val jdn = activity.appPrefs.getJdnOrNull(key) ?: Jdn.today
                    showDayPickerDialog(activity, jdn, R.string.accept) { result ->
                        activity.appPrefs.edit { putJdn(key, result) }
                    }
                }) { title(R.string.select_date) }
                clickable(onClick = {
                    showColorPickerDialog(
                        activity, false, PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId
                    )
                }) {
                    title(R.string.widget_text_color)
                    summary(R.string.select_widgets_text_color)
                }
                clickable(onClick = {
                    showColorPickerDialog(
                        activity, true, PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId
                    )
                }) {
                    title(R.string.widget_background_color)
                    summary(R.string.select_widgets_background_color)
                }
            }
        }
    }
}
