package com.byagowi.persiancalendar.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderAlert extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Intent reminderIntent = new Intent(context, ReminderActivity.class);
        //reminderIntent.putExtras(intent.getExtras());
        //reminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
        //		| Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startActivity(reminderIntent);
        Intent i = new Intent(context, ReminderNotification.class);
        Bundle extras = intent.getExtras();
        if (extras != null)
            i.putExtras(extras);
        context.startService(i);
    }
}