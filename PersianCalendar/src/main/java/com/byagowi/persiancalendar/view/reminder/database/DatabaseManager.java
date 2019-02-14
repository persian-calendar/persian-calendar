package com.byagowi.persiancalendar.view.reminder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;
import com.byagowi.persiancalendar.view.reminder.utils.Reminder;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DatabaseManager {

	private Context context;
	private SQLiteDatabase db;

	private ReminderAction eventsDAO;

	public DatabaseManager(Context context) {


		this.context = context;

		SQLiteOpenHelper openHelper = new OpenHelper(this.context);
		db = openHelper.getWritableDatabase();

		eventsDAO = new ReminderAction(db);
	}

	public SQLiteDatabase gedDb() {
		return db;
	}

	public ReminderDetails[] getAllEvents() {
		return eventsDAO.getAll();
	}

	public void removeEvent(long id) {
		Reminder.turnOFF(context, id);
		eventsDAO.remove(id);
	}

	public void saveEvent(ReminderDetails event) {
		long id = event.getId();
		if (id < 0)
			event.setId(eventsDAO.insert(event));
		else
			eventsDAO.update(event);
		Reminder.turnON(context, event);
	}

	public ReminderDetails getEvent(long id) {
		return eventsDAO.get(id);
	}

}
