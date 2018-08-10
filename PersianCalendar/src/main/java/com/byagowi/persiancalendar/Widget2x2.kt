package com.byagowi.persiancalendar

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

import com.byagowi.persiancalendar.util.UpdateUtils
import com.byagowi.persiancalendar.util.Utils

class Widget2x2 : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        Utils.startEitherServiceOrWorker(context)
        UpdateUtils.update(context, false)
    }
}
