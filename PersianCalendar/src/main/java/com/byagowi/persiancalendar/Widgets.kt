package com.byagowi.persiancalendar

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

import com.byagowi.persiancalendar.utils.UpdateUtils
import com.byagowi.persiancalendar.utils.Utils

abstract class WidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        Utils.startEitherServiceOrWorker(context)
        UpdateUtils.update(context, false)
    }
}

class Widget1x1 : WidgetProvider()
class Widget2x2 : WidgetProvider()
class Widget4x1 : WidgetProvider()
class Widget4x2 : WidgetProvider()
