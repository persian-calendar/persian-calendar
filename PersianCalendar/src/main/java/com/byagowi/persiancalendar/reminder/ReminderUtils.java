package com.byagowi.persiancalendar.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.reminder.model.Reminder;
import com.byagowi.persiancalendar.service.ReminderAlert;

import java.util.concurrent.TimeUnit;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderUtils {

    public static void turnOn(Context context, Reminder event) {
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

    public static int unitToOrdination(TimeUnit unit) {
        switch (unit) {
            case MINUTES:
                return 0;
            case HOURS:
                return 1;
            default:
            case DAYS:
                return 2;
        }
    }

    public static TimeUnit ordinationToUnit(int ordination) {
        switch (ordination) {
            case 0:
                return TimeUnit.MINUTES;
            case 1:
                return TimeUnit.HOURS;
            default:
            case 2:
                return TimeUnit.DAYS;
        }
    }
}
