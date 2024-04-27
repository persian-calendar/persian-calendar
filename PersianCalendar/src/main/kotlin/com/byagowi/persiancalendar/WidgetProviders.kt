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

    // onReceive will be called on any kind of calls to widget provider
    // such as onUpdate so no need to implement that specifically
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        context ?: return
        startWorker(context)
        update(context, false)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        // set updateDate to make sure it passes throttle and gets updated
        update(context ?: return, true)
    }
}

class Widget1x1 : WidgetProvider()
class Widget2x2 : WidgetProvider()
class Widget4x1 : WidgetProvider()
class Widget4x2 : WidgetProvider()
class WidgetMap : WidgetProvider()
class WidgetMoon : WidgetProvider()
class WidgetSunView : WidgetProvider()
class WidgetMonthView : WidgetProvider()

class AgeWidget : WidgetProvider() {
    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (context == null || appWidgetIds == null || appWidgetIds.isEmpty()) return
        context.preferences.edit {
            appWidgetIds.forEach {
                remove(PREF_SELECTED_WIDGET_BACKGROUND_COLOR + it)
                remove(PREF_SELECTED_WIDGET_TEXT_COLOR + it)
                remove(PREF_SELECTED_DATE_AGE_WIDGET + it)
                remove(PREF_TITLE_AGE_WIDGET + it)
            }
        }
    }
}
