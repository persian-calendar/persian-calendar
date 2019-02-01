package com.byagowi.persiancalendar.view.drugalert.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import com.byagowi.persiancalendar.view.drugalert.model.DrugDetails;
import com.byagowi.persiancalendar.view.drugalert.model.DrugUnit;
import com.byagowi.persiancalendar.view.drugalert.utils.Utils;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DrugAction implements DatabaseAction<DrugDetails> {

	private static final String INSERT = "insert into "
			+ DrugTable.TABLE_NAME + "(" + DrugTable.EventsColumns._ID + ", "
			+ DrugTable.EventsColumns.NAME + ", " + DrugTable.EventsColumns.INFO + ", "
			+ DrugTable.EventsColumns.PERIOD + ", " + DrugTable.EventsColumns.PERIOD_UNIT + ", "
			+ DrugTable.EventsColumns.START_TIME + ") values (?, ?, ?, ?, ?, ?)";

	private SQLiteDatabase db;
	private SQLiteStatement insertStatement;

	DrugAction(SQLiteDatabase db) {
		this.db = db;
		insertStatement = db.compileStatement(DrugAction.INSERT);
	}

	@Override
	public long insert(DrugDetails event) {
		insertStatement.clearBindings();
		insertStatement.bindNull(1);
		insertStatement.bindString(2, event.getDrugName());
		insertStatement.bindString(3, event.getDrugInfo());
		insertStatement.bindLong(4, event.getDrugPeriod().getQuantity());
		insertStatement.bindString(5, event.getDrugPeriod().getUnit());
		insertStatement.bindString(6, Utils.dateToString(event.getStartTime()));
		return insertStatement.executeInsert();
	}

	@Override
	public void update(DrugDetails event) {
		final ContentValues values = new ContentValues();
		values.put(DrugTable.EventsColumns.NAME, event.getDrugName());
		values.put(DrugTable.EventsColumns.INFO, event.getDrugInfo());
		values.put(DrugTable.EventsColumns.PERIOD, event.getDrugPeriod().getQuantity());
		values.put(DrugTable.EventsColumns.PERIOD_UNIT, event.getDrugPeriod().getUnit());
		values.put(DrugTable.EventsColumns.START_TIME,
				Utils.dateToString(event.getStartTime()));
		db.update(DrugTable.TABLE_NAME, values, BaseColumns._ID + " = ?",
				new String[] { String.valueOf(event.getId()) });

	}

	@Override
	public void remove(long id) {
		db.delete(DrugTable.TABLE_NAME, BaseColumns._ID + " = ?",
				new String[] { String.valueOf(id) });
	}

	@Override
	public DrugDetails get(long id) {
		DrugDetails event = null;
		Cursor c = db.query(DrugTable.TABLE_NAME, new String[] {
				DrugTable.EventsColumns.NAME, DrugTable.EventsColumns.INFO, DrugTable.EventsColumns.PERIOD,
				DrugTable.EventsColumns.PERIOD_UNIT, DrugTable.EventsColumns.START_TIME },
				BaseColumns._ID + " = ?", new String[] { String.valueOf(id) },
				null, null, null);
		if (c.moveToFirst()) {
			event = new DrugDetails();
			event.setId(id);
			event.setDrugName(c.getString(0));
			event.setDrugInfo(c.getString(1));
			event.setDrugPeriod(new DrugUnit(c.getInt(2), c.getString(3)));
			event.setStartTime(Utils.stringToDate(c.getString(4)));
		}
		if (!c.isClosed()) {
			c.close();
		}
		return event;
	}

	@Override
	public DrugDetails[] getAll() {
		DrugDetails[] events = null;
		Cursor c = db.query(DrugTable.TABLE_NAME, new String[] {
				DrugTable.EventsColumns._ID, DrugTable.EventsColumns.NAME, DrugTable.EventsColumns.INFO,
				DrugTable.EventsColumns.PERIOD, DrugTable.EventsColumns.PERIOD_UNIT,
				DrugTable.EventsColumns.START_TIME }, null, null, null, null, null);
		if (c.moveToFirst()) {
			events = new DrugDetails[c.getCount()];
			for (int i = 0; i < c.getCount(); i++) {
				events[i] = new DrugDetails();
				events[i].setId(c.getLong(0));
				events[i].setDrugName(c.getString(1));
				events[i].setDrugInfo(c.getString(2));
				events[i].setDrugPeriod(new DrugUnit(c.getInt(3), c.getString(4)));
				events[i].setStartTime(Utils.stringToDate(c.getString(5)));
				c.moveToNext();
			}
		}
		if (!c.isClosed()) {
			c.close();
		}
		return events;
	}

}
