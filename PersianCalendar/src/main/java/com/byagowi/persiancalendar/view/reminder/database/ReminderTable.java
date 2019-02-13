package com.byagowi.persiancalendar.view.reminder.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class ReminderTable {
	
		static final String TABLE_NAME = "Events";
		static class EventsColumns implements BaseColumns {
		static final String NAME = "name";
		static final String INFO = "info";
		static final String PERIOD = "period";
		static final String PERIOD_UNIT = "per_unit";
		static final String START_TIME = "start_time";
	}
	
	static void onCreate(SQLiteDatabase db) {
		String sb = ("CREATE TABLE " + ReminderTable.TABLE_NAME + " (") +
				BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				EventsColumns.NAME + " TEXT, " +
				EventsColumns.INFO + " TEXT, " +
				EventsColumns.PERIOD + " INTEGER, " +
				EventsColumns.PERIOD_UNIT + " TEXT, " +
				EventsColumns.START_TIME + " TEXT" +
				");";
		db.execSQL(sb);
	}
	
	static void onUpgrade(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + ReminderTable.TABLE_NAME);
		ReminderTable.onCreate(db);
	}
	
}
