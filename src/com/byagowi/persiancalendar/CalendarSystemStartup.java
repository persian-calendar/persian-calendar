package com.byagowi.persiancalendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Startup broadcast receiver
 *
 * @author ebraminio
 */
public class CalendarSystemStartup extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {    	
        Intent startServiceIntent = new Intent(context, CalendarService.class);
        context.startService(startServiceIntent);
    }
}
