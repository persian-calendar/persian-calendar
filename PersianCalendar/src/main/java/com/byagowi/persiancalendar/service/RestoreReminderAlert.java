package com.byagowi.persiancalendar.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.reminder.model.ReminderDetails;
import com.byagowi.persiancalendar.reminder.utils.Reminder;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class RestoreReminderAlert extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        for (ReminderDetails event : Utils.getReminderDetails()) {
            Reminder.turnOn(this, event);
        }
        return START_NOT_STICKY;
    }

}
