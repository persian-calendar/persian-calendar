package com.byagowi.persiancalendar.view.drugalert.database;

import com.byagowi.persiancalendar.view.drugalert.constants.Constants;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class OpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	
	OpenHelper(final Context context) {
		super(context, Constants.DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void onOpen(final SQLiteDatabase db) {
		super.onOpen(db);
		if(!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys=ON;");
			Cursor c =db.rawQuery("PRAGMA foreign_keys", null);
			if(c.moveToFirst()) {
				int result = c.getInt(0);
			}
			if(!c.isClosed()) {
				c.close();
			}
		}
	}
	
	public void onCreate(final SQLiteDatabase db) {
		DrugTable.onCreate(db);
	}
	
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		DrugTable.onUpgrade(db);
	}

}