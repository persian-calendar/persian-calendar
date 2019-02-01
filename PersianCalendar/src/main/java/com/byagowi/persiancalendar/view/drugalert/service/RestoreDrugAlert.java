package com.byagowi.persiancalendar.view.drugalert.service;

import com.byagowi.persiancalendar.view.drugalert.application.DrugAlertApplication;
import com.byagowi.persiancalendar.view.drugalert.model.DrugDetails;
import com.byagowi.persiancalendar.view.drugalert.utils.Reminder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class RestoreDrugAlert extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		DrugAlertApplication app = (DrugAlertApplication) getApplication();
		DrugDetails[] events = app.getDatabaseManager().getAllEvents();
		if (events != null) {
			for (DrugDetails event : events) {
				Reminder.turnON(this, event);
			}
		}
		return START_NOT_STICKY;
	}

}
