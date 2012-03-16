package com.byagowi.persiancalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;

import com.byagowi.persiancalendar.R;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 * Program widget on Android launcher.
 * 
 * @author ebraminio
 * 
 */
public class PersianCalendarWidget1x1 extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		updateTime(context);
	}

	static public void updateTime(Context context) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean persianDigit = prefs.getBoolean("PersianDigits", true);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget1x1);

		CivilDate civil = new CivilDate();
		PersianDate persian = DateConverter.civilToPersian(civil);

		remoteViews.setTextViewText(R.id.textPlaceholder2_1x1,
				PersianCalendarUtils.textShaper(persian.getMonthName()));

		remoteViews.setTextViewText(R.id.textPlaceholder1_1x1, PersianCalendarUtils
				.formatNumber(persian.getDayOfMonth(), persianDigit));

		Intent launchAppIntent = new Intent(context,
				PersianCalendarActivity.class);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
				context, 0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_layout1x1,
				launchAppPendingIntent);

		ComponentName widget = new ComponentName(context,
				PersianCalendarWidget1x1.class);
		
		manager.updateAppWidget(widget, remoteViews);
	}
}
