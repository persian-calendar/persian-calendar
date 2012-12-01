/*
 * March 2012
 *
 * In place of a legal notice, here is a blessing:
 *
 *    May you do good and not evil.
 *    May you find forgiveness for yourself and forgive others.
 *    May you share freely, never taking more than you give.
 *
 */
package com.byagowi.persiancalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;

import com.byagowi.persiancalendar.R;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import static com.byagowi.persiancalendar.CalendarUtils.*;

/**
 * 1x1 widget provider
 * 
 * @author ebraminio
 * 
 */
public class CalendarWidget1x1 extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		updateTime(context);
	}
	

	static public void updateTime(Context context) {
		char[] digits = preferenceDigits(context);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget1x1);

		CivilDate civil = new CivilDate();
		PersianDate persian = DateConverter.civilToPersian(civil);
		persian.setDari(isDariVersion(context));
		
		remoteViews.setTextViewText(R.id.textPlaceholder2_1x1,
				textShaper(persian.getMonthName()));

		int color;
		if (blackWidget(context)) {
			color = context.getResources().getColor(android.R.color.black);
		} else {
			color = context.getResources().getColor(android.R.color.white);
		}

		remoteViews.setTextColor(R.id.textPlaceholder1_1x1, color);
		remoteViews.setTextColor(R.id.textPlaceholder2_1x1, color);
		
			
		remoteViews.setTextViewText(R.id.textPlaceholder1_1x1, formatNumber(persian.getDayOfMonth(), digits));

		Intent launchAppIntent = new Intent(context,
				CalendarActivity.class);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
				context, 0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_layout1x1,
				launchAppPendingIntent);

		ComponentName widget = new ComponentName(context,
				CalendarWidget1x1.class);
		
		manager.updateAppWidget(widget, remoteViews);
	}
}
