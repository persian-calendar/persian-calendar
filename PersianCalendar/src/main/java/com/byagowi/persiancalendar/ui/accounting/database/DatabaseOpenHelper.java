package com.byagowi.persiancalendar.ui.accounting.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String dbName = "accounting";

    DatabaseOpenHelper(Context context) {
        super(context, dbName, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Table_Params ( pID INTEGER PRIMARY KEY AUTOINCREMENT , pName TEXT UNIQUE , pStat INTEGER );");
        db.execSQL("CREATE TABLE IF NOT EXISTS Table_Account (aID INTEGER PRIMARY KEY AUTOINCREMENT , aParamID INTEGER , aPrice INTEGER , aStat INTEGER , aDate TEXT , FOREIGN KEY(aParamID) REFERENCES Table_Params (pID) );");
        db.execSQL("CREATE TABLE IF NOT EXISTS Table_User ( oldPass TEXT , newPass TEXT , activeLogin INTEGER , question TEXT , ans TEXT );");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS accounting");
        onCreate(db);
    }
}
