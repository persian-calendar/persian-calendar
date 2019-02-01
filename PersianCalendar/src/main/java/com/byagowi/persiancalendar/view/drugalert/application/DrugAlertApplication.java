package com.byagowi.persiancalendar.view.drugalert.application;


import android.annotation.SuppressLint;
import android.app.Application;

import com.byagowi.persiancalendar.view.drugalert.database.DatabaseManager;


/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
@SuppressLint("SdCardPath")
public class DrugAlertApplication extends Application {

	private DatabaseManager databaseManager;

	public DatabaseManager getDatabaseManager() {
		return this.databaseManager;
	}
	public void onCreate() {
		super.onCreate();
		databaseManager = new DatabaseManager(this);
	}
}
