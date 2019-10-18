package com.byagowi.persiancalendar

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

import com.byagowi.persiancalendar.utils.Utils
import com.byagowi.persiancalendar.utils.update

abstract class WidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        Utils.startEitherServiceOrWorker(context)
        update(context, false)
    }
}

class Widget1x1 : WidgetProvider()
class Widget2x2 : WidgetProvider()
class Widget4x1 : WidgetProvider()
class Widget4x2 : WidgetProvider()
