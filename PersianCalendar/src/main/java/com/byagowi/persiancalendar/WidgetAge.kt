package com.byagowi.persiancalendar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.edit
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculateDaysDifference

class AgeWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        context ?: return
        appWidgetIds.forEach { updateAgeWidget(context, appWidgetManager, it) }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray) {
        context ?: return
        if (appWidgetIds.isEmpty()) return
        context.appPrefs.edit {
            appWidgetIds.forEach {
                remove(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + it)
                remove(PREF_SELECTED_WIDGET_TEXT_COLOR + it)
                remove(PREF_SELECTED_DATE_AGE_WIDGET + it)
                remove(PREF_TITLE_AGE_WIDGET + it)
            }
        }
    }

    override fun onEnabled(context: Context) = Unit
    override fun onDisabled(context: Context) = Unit
}

internal fun updateAgeWidget(
    context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int
) {
    applyAppLanguage(context)

    val sharedPreferences = context.appPrefs

    val jdn = sharedPreferences.getLong(PREF_SELECTED_DATE_AGE_WIDGET + appWidgetId, 0)
    val views = RemoteViews(context.packageName, R.layout.widget_age)
    views.setTextViewText(
        R.id.textview_age_widget,
        calculateDaysDifference(jdn, context.getString(R.string.age_widget_placeholder))
    )
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
