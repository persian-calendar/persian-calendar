package com.byagowi.persiancalendar.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.byagowi.persiancalendar.view.reminder.database.DatabaseManager;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;
import com.byagowi.persiancalendar.view.reminder.utils.Reminder;

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
		DatabaseManager databaseManager = new DatabaseManager(this);

		ReminderDetails[] events = databaseManager.getAllEvents();
		if (events != null) {
			for (ReminderDetails event : events) {
				Reminder.turnON(this, event);
			}
		}
		return START_NOT_STICKY;
	}

}
