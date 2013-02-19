package com.byagowi.persiancalendar;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * Abstract widget class
 *
 * @author ebraminio
 */
abstract class CalendarWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, CalendarService.class);
        context.startService(startServiceIntent);
    }
}
