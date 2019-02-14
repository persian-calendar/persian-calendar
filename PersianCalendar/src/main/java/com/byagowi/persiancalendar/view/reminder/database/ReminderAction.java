package com.byagowi.persiancalendar.view.reminder.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;
import com.byagowi.persiancalendar.view.reminder.model.ReminderUnit;
import com.byagowi.persiancalendar.view.reminder.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderAction implements DatabaseAction<ReminderDetails> {

	private static final String INSERT = "insert into "
			+ ReminderTable.TABLE_NAME + "(" + ReminderTable.EventsColumns._ID + ", "
			+ ReminderTable.EventsColumns.NAME + ", " + ReminderTable.EventsColumns.INFO + ", "
			+ ReminderTable.EventsColumns.PERIOD + ", " + ReminderTable.EventsColumns.PERIOD_UNIT + ", "
			+ ReminderTable.EventsColumns.START_TIME + ") values (?, ?, ?, ?, ?, ?)";

	private SQLiteDatabase db;
	private SQLiteStatement insertStatement;

	ReminderAction(SQLiteDatabase db) {
		this.db = db;
		insertStatement = db.compileStatement(ReminderAction.INSERT);
	}

	@Override
	public long insert(ReminderDetails event) {
		insertStatement.clearBindings();
		insertStatement.bindNull(1);
		insertStatement.bindString(2, event.getReminderName());
		insertStatement.bindString(3, event.getReminderInfo());
		insertStatement.bindLong(4, event.getReminderPeriod().getQuantity());
		insertStatement.bindString(5, event.getReminderPeriod().getUnit());
		insertStatement.bindString(6, Utils.dateToString(event.getStartTime()));
		return insertStatement.executeInsert();
	}

	@Override
	public void update(ReminderDetails event) {
		final ContentValues values = new ContentValues();
		values.put(ReminderTable.EventsColumns.NAME, event.getReminderName());
		values.put(ReminderTable.EventsColumns.INFO, event.getReminderInfo());
		values.put(ReminderTable.EventsColumns.PERIOD, event.getReminderPeriod().getQuantity());
		values.put(ReminderTable.EventsColumns.PERIOD_UNIT, event.getReminderPeriod().getUnit());
		values.put(ReminderTable.EventsColumns.START_TIME,
				Utils.dateToString(event.getStartTime()));
		db.update(ReminderTable.TABLE_NAME, values, BaseColumns._ID + " = ?",
				new String[] { String.valueOf(event.getId()) });

	}

	@Override
	public void remove(long id) {
		db.delete(ReminderTable.TABLE_NAME, BaseColumns._ID + " = ?",
				new String[] { String.valueOf(id) });
	}

	@Override
	public ReminderDetails get(long id) {
		ReminderDetails event = null;
		Cursor c = db.query(ReminderTable.TABLE_NAME, new String[] {
				ReminderTable.EventsColumns.NAME, ReminderTable.EventsColumns.INFO, ReminderTable.EventsColumns.PERIOD,
				ReminderTable.EventsColumns.PERIOD_UNIT, ReminderTable.EventsColumns.START_TIME },
				BaseColumns._ID + " = ?", new String[] { String.valueOf(id) },
				null, null, null);
		if (c.moveToFirst()) {
			event = new ReminderDetails();
			event.setId(id);
			event.setReminderName(c.getString(0));
			event.setReminderInfo(c.getString(1));
			event.setReminderPeriod(new ReminderUnit(c.getInt(2), c.getString(3)));
			event.setStartTime(Utils.stringToDate(c.getString(4)));
		}
		if (!c.isClosed()) {
			c.close();
		}
		return event;
	}

	@Override
	public ReminderDetails[] getAll() {
		List<ReminderDetails> result = new ArrayList<>();

		Cursor c = db.query(ReminderTable.TABLE_NAME, new String[] {
				ReminderTable.EventsColumns._ID, ReminderTable.EventsColumns.NAME, ReminderTable.EventsColumns.INFO,
				ReminderTable.EventsColumns.PERIOD, ReminderTable.EventsColumns.PERIOD_UNIT,
				ReminderTable.EventsColumns.START_TIME }, null, null, null, null, null);
		if (c.moveToFirst()) {
			for (int i = 0; i < c.getCount(); i++) {
				ReminderDetails reminder = new ReminderDetails();
				reminder.setId(c.getLong(0));
				reminder.setReminderName(c.getString(1));
				reminder.setReminderInfo(c.getString(2));
				reminder.setReminderPeriod(new ReminderUnit(c.getInt(3), c.getString(4)));
				reminder.setStartTime(Utils.stringToDate(c.getString(5)));
				result.add(reminder);
				c.moveToNext();
			}
		}
		if (!c.isClosed()) {
			c.close();
		}
		return result.toArray(new ReminderDetails[0]);
	}

}
