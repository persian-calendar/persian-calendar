package com.byagowi.persiancalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Startup broadcast receiver
 *
 * @author ebraminio
 */
public class SystemStartup extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ApplicationService.class));
    }
}
