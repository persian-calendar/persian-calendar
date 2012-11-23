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

import java.util.Date;

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

import static com.byagowi.persiancalendar.CalendarUtils.*;

/**
 * 4x1 widget provider
 * 
 * @author ebraminio
 * 
 */
public class CalendarWidget4x1 extends AppWidgetProvider {
	static private IntentFilter intentFilter = null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		if (intentFilter == null) {
			intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
			context.getApplicationContext().registerReceiver(
					new BroadcastReceiver() {
						@Override
						public void onReceive(Context context, Intent intent) {
							updateTime(context);
						}
					}, intentFilter);
		}

		updateTime(context);
	}

	static public void updateTime(Context context) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean gadgetClock = prefs.getBoolean("GadgetClock", false);

		char[] digits = preferenceDigits(context);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget4x1);

		CivilDate civil = new CivilDate();
		PersianDate persian = DateConverter.civilToPersian(civil);
		persian.setDari(isDariVersion(context));

		String text1;
		String text2;

		text1 = getDayOfWeekName(civil.getDayOfWeek());
		text2 = dateToString(persian, digits, true) + PERSIAN_COMMA + " "
				+ dateToString(civil, digits, true);
		if (gadgetClock) {
			text2 = text1 + " " + text2;
			text1 = getPersianFormattedClock(new Date(), digits, false);
		}

		text1 = textShaper(text1);
		text2 = textShaper(text2);

		int color;
		if (blackWidget(context)) {
			color = context.getResources().getColor(android.R.color.black);
		} else {
			color = context.getResources().getColor(android.R.color.white);
		}

		remoteViews.setTextColor(R.id.textPlaceholder1_4x1, color);
		remoteViews.setTextColor(R.id.textPlaceholder2_4x1, color);

		remoteViews.setTextViewText(R.id.textPlaceholder1_4x1, text1);
		remoteViews.setTextViewText(R.id.textPlaceholder2_4x1, text2);

		Intent launchAppIntent = new Intent(context, CalendarActivity.class);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
				context, 0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_layout4x1,
				launchAppPendingIntent);

		ComponentName widget = new ComponentName(context,
				CalendarWidget4x1.class);
		manager.updateAppWidget(widget, remoteViews);
	}
}
