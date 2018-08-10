package com.byagowi.persiancalendar

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

import com.byagowi.persiancalendar.util.UpdateUtils
import com.byagowi.persiancalendar.util.Utils

/**
 * 4x1 widget provider, implementation is on `CalendarWidget`
 *
 * @author ebraminio
 */
class Widget4x1 : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        Utils.startEitherServiceOrWorker(context)
        UpdateUtils.update(context, false)
    }
}
