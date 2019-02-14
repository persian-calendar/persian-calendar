package com.byagowi.persiancalendar.view.reminder.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.byagowi.persiancalendar.service.ReminderAlert;
import com.byagowi.persiancalendar.view.reminder.constants.Constants;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class Reminder {

    public static void turnOn(Context context, ReminderDetails event) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long startTime = event.startTime;
        long period = event.unit.toMillis(1);
        // FIXME: BAD IDEA
        // while (startTime < System.currentTimeMillis()) startTime += period;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, period,
                prepareIntent(context, event.id));

    }

    public static void turnOff(Context context, long event_id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        alarmManager.cancel(prepareIntent(context, event_id));
    }

    private static PendingIntent prepareIntent(Context context, long event_id) {
        Intent intent = new Intent(context, ReminderAlert.class);
        intent.setAction(String.valueOf(event_id));
        intent.putExtra(Constants.REMINDER_ID, event_id);
        return PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
