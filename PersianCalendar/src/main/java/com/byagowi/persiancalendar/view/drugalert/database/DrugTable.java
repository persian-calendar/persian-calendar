package com.byagowi.persiancalendar.view.drugalert.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class DrugTable {
	
		static final String TABLE_NAME = "Events";
		static class EventsColumns implements BaseColumns {
		static final String NAME = "name";
		static final String INFO = "info";
		static final String PERIOD = "period";
		static final String PERIOD_UNIT = "per_unit";
		static final String START_TIME = "start_time";
	}
	
	static void onCreate(SQLiteDatabase db) {
		String sb = ("CREATE TABLE " + DrugTable.TABLE_NAME + " (") +
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
		db.execSQL("DROP TABLE IF EXISTS " + DrugTable.TABLE_NAME);
		DrugTable.onCreate(db);
	}
	
}
