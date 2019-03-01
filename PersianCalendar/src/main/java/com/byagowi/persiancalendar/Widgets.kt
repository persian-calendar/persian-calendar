package com.byagowi.persiancalendar

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.annotation.Nullable

import com.byagowi.persiancalendar.utils.UpdateUtils
import com.byagowi.persiancalendar.utils.Utils

class Widget1x1 : AppWidgetProvider() {
    override fun onReceive(context: Context, @Nullable intent: Intent) {
        Utils.startEitherServiceOrWorker(context)
        UpdateUtils.update(context, false)
    }
}

class Widget2x2 : AppWidgetProvider() {
    override fun onReceive(context: Context, @Nullable intent: Intent) {
        Utils.startEitherServiceOrWorker(context)
        UpdateUtils.update(context, false)
    }
}

class Widget4x1 : AppWidgetProvider() {
    override fun onReceive(context: Context, @Nullable intent: Intent) {
        Utils.startEitherServiceOrWorker(context)
        UpdateUtils.update(context, false)
    }
}

class Widget4x2 : AppWidgetProvider() {
    override fun onReceive(context: Context, @Nullable intent: Intent) {
        Utils.startEitherServiceOrWorker(context)
        UpdateUtils.update(context, false)
    }
}
