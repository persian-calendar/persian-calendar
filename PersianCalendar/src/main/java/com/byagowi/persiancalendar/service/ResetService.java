package com.byagowi.persiancalendar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ResetService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent restoreIntent = new Intent(context, RestoreReminderAlert.class);
        context.startService(restoreIntent);
    }

}
