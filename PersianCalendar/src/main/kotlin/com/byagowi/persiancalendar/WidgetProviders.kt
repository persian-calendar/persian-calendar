package com.byagowi.persiancalendar

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.startEitherServiceOrWorker
import com.byagowi.persiancalendar.utils.update

abstract class WidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        startEitherServiceOrWorker(context)
        update(context, false)
    }
}

class Widget1x1 : WidgetProvider()
class Widget2x2 : WidgetProvider()
class Widget4x1 : WidgetProvider()
class Widget4x2 : WidgetProvider()
class WidgetSunView : WidgetProvider()
//class Widget4x1dateOnly : WidgetProvider()

class AgeWidget : WidgetProvider() {
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
}
