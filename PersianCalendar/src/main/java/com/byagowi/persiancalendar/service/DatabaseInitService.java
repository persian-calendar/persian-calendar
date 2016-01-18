package com.byagowi.persiancalendar.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.db.LocationsHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
public class DatabaseInitService extends IntentService {
    private static final String TAG = "DatabaseInitService";

    public DatabaseInitService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LocationsHelper helper = new LocationsHelper(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        String line;
        InputStreamReader isr;
        BufferedReader bufferedReader = null;
        try {
            // copy countries to database
            if (helper.countCountries() == 0) {
                isr = new InputStreamReader(getResources().openRawResource(R.raw.countries));
                bufferedReader = new BufferedReader(isr);

                // db is closed with count
                db = helper.getWritableDatabase();
                while ((line = bufferedReader.readLine()) != null) {
                    String[] parts = line.split("\\;");
                    ContentValues cv = new ContentValues();
                    cv.put(LocationsHelper.COLUMN_KEY, parts[0]);
                    cv.put(LocationsHelper.COLUMN_NAME_EN, parts[1]);
                    cv.put(LocationsHelper.COLUMN_NAME_FA, parts[2]);
                    db.insert(LocationsHelper.TABLE_NAME_COUNTRIES, null, cv);
                }
            }

            // copy cities to database
            if (helper.countCities() == 0) {
                isr = new InputStreamReader(getResources().openRawResource(R.raw.cities));
                bufferedReader = new BufferedReader(isr);

                db = helper.getWritableDatabase();
                while ((line = bufferedReader.readLine()) != null) {
                    String[] parts = line.split("\\;");
                    ContentValues cv = new ContentValues();
                    cv.put(LocationsHelper.COLUMN_KEY, parts[0]);
                    cv.put(LocationsHelper.COLUMN_NAME_EN, parts[1]);
                    cv.put(LocationsHelper.COLUMN_NAME_FA, parts[2]);
                    cv.put(LocationsHelper.COLUMN_COUNTRY, parts[3]);
                    db.insert(LocationsHelper.TABLE_NAME_CITIES, null, cv);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
                db.close();
            } catch (IOException ignored) {
            }
        }

        stopSelf();
    }
}
