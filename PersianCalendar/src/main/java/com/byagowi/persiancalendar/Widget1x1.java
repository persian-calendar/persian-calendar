package com.byagowi.persiancalendar;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.util.UpdateUtils;
import com.byagowi.persiancalendar.util.Utils;

/**
 * 1x1 widget provider, implementation is on {@code CalendarWidget}
 *
 * @author ebraminio
 */
public class Widget1x1 extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Utils.isServiceRunning(context, ApplicationService.class)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(new Intent(context, ApplicationService.class));
            context.startService(new Intent(context, ApplicationService.class));
        }
        UpdateUtils.update(context, true);
    }
}
