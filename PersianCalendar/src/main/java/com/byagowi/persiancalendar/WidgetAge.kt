package com.byagowi.persiancalendar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.edit
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getTodayJdn
import io.github.persiancalendar.calendar.CivilDate
import kotlin.math.abs

class AgeWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            context.appPrefs.edit {
                remove(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId)
                remove(PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId)
                remove(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId)
                remove(PREF_TITLE_AGE_WIDGET + appWidgetId)
            }
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }
}
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val sharedPreferences = context.appPrefs

    val jdn = sharedPreferences.getLong(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId, 0)
    val selectedDayAbsoluteDistance = abs(getTodayJdn() - jdn)
    val civilBase = CivilDate(2000, 1, 1)
    val civilOffset = CivilDate(civilBase.toJdn() + selectedDayAbsoluteDistance)
    val yearDiff = civilOffset.year - 2000
    val monthDiff = civilOffset.month - 1
    val dayOfMonthDiff = civilOffset.dayOfMonth - 1

    var text = context.getString(R.string.age_widget_placeholder).format(
        formatNumber(selectedDayAbsoluteDistance.toInt()).toInt(),
        formatNumber(yearDiff).toInt(),
        formatNumber(monthDiff).toInt(),
        formatNumber(dayOfMonthDiff).toInt()
    )

    if (selectedDayAbsoluteDistance <= 31) text = text.split(" (")[0]

    val views = RemoteViews(context.packageName, R.layout.widget_age)

    views.setTextViewText(R.id.textview_age_widget, text)
    val textColor = sharedPreferences.getString(
        PREF_SELECTED_WIDGET_TEXT_COLOR + appWidgetId,
        DEFAULT_SELECTED_WIDGET_TEXT_COLOR
    )
    val bgColor = sharedPreferences.getString(
        PREF_SELECTED_WIDGET_BACKGROUND_COLOR + appWidgetId,
        DEFAULT_SELECTED_WIDGET_BACKGROUND_COLOR
    )

    val title = sharedPreferences.getString(PREF_TITLE_AGE_WIDGET + appWidgetId, "")
    if (title.isNullOrEmpty()) {
        views.setInt(R.id.textview_age_widget_title, "setVisibility", View.GONE)
    } else {
        views.setTextViewText(R.id.textview_age_widget_title, title)
        views.setTextColor(R.id.textview_age_widget_title, Color.parseColor(textColor))
    }

    views.setTextColor(R.id.textview_age_widget, Color.parseColor(textColor))
    views.setInt(R.id.age_widget_root, "setBackgroundColor", Color.parseColor(bgColor))

    appWidgetManager.updateAppWidget(appWidgetId, views)
}