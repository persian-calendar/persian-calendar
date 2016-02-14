package com.byagowi.persiancalendar.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
public class LocationsHelper extends SQLiteOpenHelper {
    private static final String TAG = "LocationsHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PersianCalendar";
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_NAME_EN = "name_en";
    public static final String COLUMN_NAME_FA = "name_fa";
    public static final String COLUMN_COUNTRY = "country";

    public static final String TABLE_NAME_COUNTRIES = "countries";
    public static final String SQL_CREATE_TABLE_COUNTRIES = "CREATE TABLE " + TABLE_NAME_COUNTRIES + "(" +
            BaseColumns._ID + " INTEGER PRIMARY KEY," +
            COLUMN_KEY + " TEXT UNIQUE, " +
            COLUMN_NAME_EN + " TEXT UNIQUE, " +
            COLUMN_NAME_FA + " TEXT UNIQUE" +
            ")";

    public static final String TABLE_NAME_CITIES = "cities";
    public static final String SQL_CREATE_TABLE_CITIES = "CREATE TABLE " + TABLE_NAME_CITIES + "(" +
            BaseColumns._ID + " INTEGER PRIMARY KEY, " +
            COLUMN_COUNTRY + " TEXT REFERENCES " + TABLE_NAME_COUNTRIES + "(" + COLUMN_KEY + "), " +
            COLUMN_KEY + " TEXT, " +
            COLUMN_NAME_EN + " TEXT, " +
            COLUMN_NAME_FA + " TEXT " +
            ")";

    public LocationsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_COUNTRIES);
        db.execSQL(SQL_CREATE_TABLE_CITIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int countCountries() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME_COUNTRIES, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int countCities() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME_CITIES, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public Cursor listCities(String langCode) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT " +
                "i." + BaseColumns._ID + " " + BaseColumns._ID + ", " +
                "i." + COLUMN_KEY + " " + TABLE_NAME_CITIES + COLUMN_KEY + ", " +
                "i." + COLUMN_COUNTRY + " " + TABLE_NAME_CITIES + COLUMN_COUNTRY + ", " +
                "i." + COLUMN_NAME_EN + " " + TABLE_NAME_CITIES + COLUMN_NAME_EN + ", " +
                "i." + COLUMN_NAME_FA + " " + TABLE_NAME_CITIES + COLUMN_NAME_FA + ", " +
                "c." + BaseColumns._ID + " " + TABLE_NAME_COUNTRIES + BaseColumns._ID + ", " +
                "c." + COLUMN_KEY + " " + TABLE_NAME_COUNTRIES + COLUMN_KEY + ", " +
                "c." + COLUMN_NAME_EN + " " + TABLE_NAME_COUNTRIES + COLUMN_NAME_EN + ", " +
                "c." + COLUMN_NAME_FA + " " + TABLE_NAME_COUNTRIES + COLUMN_NAME_FA + " " +
                "FROM " + TABLE_NAME_CITIES + " as i, " + TABLE_NAME_COUNTRIES + " as c " +
                "WHERE i." + COLUMN_COUNTRY + " = c." + COLUMN_KEY + " " +
                "ORDER BY " + TABLE_NAME_COUNTRIES + COLUMN_KEY + " DESC";
        if (!TextUtils.isEmpty(langCode) && langCode.equalsIgnoreCase("fa")) {
            sql += ", " + TABLE_NAME_CITIES + COLUMN_NAME_FA;
        } else {
            sql += ", " + TABLE_NAME_CITIES + COLUMN_NAME_EN;
        }

        Log.v(TAG, "running sql: " + sql);
        return db.rawQuery(sql, null);
    }
}
