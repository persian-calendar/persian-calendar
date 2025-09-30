package com.byagowi.persiancalendar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.edit
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update

abstract class WidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        context?.let {
            try {
                startWorker(it)
                update(it, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        context?.let {
            try {
                update(it, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun forceUpdateAllWidgets(context: Context?, widgetIds: IntArray?) {
        context?.let {
            widgetIds?.forEach { id ->
                try {
                    update(it, false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun resetWidgetPreferences(context: Context?, widgetIds: IntArray?) {
        context?.preferences?.edit {
            widgetIds?.forEach { id ->
                remove(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + id)
                remove(PREF_SELECTED_WIDGET_TEXT_COLOR + id)
                remove(PREF_SELECTED_DATE_AGE_WIDGET + id)
                remove(PREF_SELECTED_DATE_AGE_WIDGET_START + id)
                remove(PREF_TITLE_AGE_WIDGET + id)
            }
        }
    }

    fun updateWidget(context: Context?, widgetId: Int, force: Boolean = false) {
        context?.let {
            try {
                update(it, force)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearWidgetPreferences(context: Context?, widgetId: Int) {
        context?.preferences?.edit {
            remove(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + widgetId)
            remove(PREF_SELECTED_WIDGET_TEXT_COLOR + widgetId)
            remove(PREF_SELECTED_DATE_AGE_WIDGET + widgetId)
            remove(PREF_SELECTED_DATE_AGE_WIDGET_START + widgetId)
            remove(PREF_TITLE_AGE_WIDGET + widgetId)
        }
    }

    // Improved UI hooks could be added here if widgets supported them
}

class Widget1x1 : WidgetProvider()
class Widget2x2 : WidgetProvider()
class Widget4x1 : WidgetProvider()
class Widget4x2 : WidgetProvider()
class WidgetMap : WidgetProvider()
class WidgetMoon : WidgetProvider()
class WidgetSunView : WidgetProvider()
class WidgetMonth : WidgetProvider()
class WidgetMonthView : WidgetProvider()
class WidgetSchedule : WidgetProvider()
class WidgetWeekView : WidgetProvider()

class AgeWidget : WidgetProvider() {
    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        context?.preferences?.edit {
            appWidgetIds?.forEach { id ->
                remove(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + id)
                remove(PREF_SELECTED_WIDGET_TEXT_COLOR + id)
                remove(PREF_SELECTED_DATE_AGE_WIDGET + id)
                remove(PREF_SELECTED_DATE_AGE_WIDGET_START + id)
                remove(PREF_TITLE_AGE_WIDGET + id)
            }
        }
    }

    fun clearSingleWidget(context: Context?, widgetId: Int) {
        clearWidgetPreferences(context, widgetId)
    }

    fun duplicateWidgetPreferences(context: Context?, sourceId: Int, targetId: Int) {
        context?.let {
            val prefs = it.preferences
            prefs.getInt(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + sourceId, 0).let { bg ->
                prefs.edit { putInt(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + targetId, bg) }
            }
            prefs.getInt(PREF_SELECTED_WIDGET_TEXT_COLOR + sourceId, 0).let { tc ->
                prefs.edit { putInt(PREF_SELECTED_WIDGET_TEXT_COLOR + targetId, tc) }
            }
            prefs.getString(PREF_SELECTED_DATE_AGE_WIDGET + sourceId, null)?.let { dw ->
                prefs.edit { putString(PREF_SELECTED_DATE_AGE_WIDGET + targetId, dw) }
            }
            prefs.getString(PREF_SELECTED_DATE_AGE_WIDGET_START + sourceId, null)?.let { ds ->
                prefs.edit { putString(PREF_SELECTED_DATE_AGE_WIDGET_START + targetId, ds) }
            }
            prefs.getString(PREF_TITLE_AGE_WIDGET + sourceId, null)?.let { t ->
                prefs.edit { putString(PREF_TITLE_AGE_WIDGET + targetId, t) }
            }
        }
    }
}
 
