package com.byagowi.persiancalendar

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
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
