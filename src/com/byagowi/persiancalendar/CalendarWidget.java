package com.byagowi.persiancalendar;

import static com.byagowi.persiancalendar.CalendarUtils.PERSIAN_COMMA;
import static com.byagowi.persiancalendar.CalendarUtils.dateToString;
import static com.byagowi.persiancalendar.CalendarUtils.formatNumber;
import static com.byagowi.persiancalendar.CalendarUtils.getDayOfWeekName;
import static com.byagowi.persiancalendar.CalendarUtils.getPersianFormattedClock;
import static com.byagowi.persiancalendar.CalendarUtils.isDariVersion;
import static com.byagowi.persiancalendar.CalendarUtils.preferredDigits;
import static com.byagowi.persiancalendar.CalendarUtils.textShaper;

import java.util.Date;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 * Abstract widget class
 * 
 * @author ebraminio
 * 
 */
abstract public class CalendarWidget extends AppWidgetProvider {
	static private IntentFilter intentFilter = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intentFilter == null) {
			intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
			context.getApplicationContext()
					.registerReceiver(this, intentFilter);
		}

		update(context);
	}

	public static void update(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean gadgetClock = prefs.getBoolean("GadgetClock", true);
		boolean gadgetIn24 = prefs.getBoolean("GadgetIn24", false);
		boolean blackWidget = prefs.getBoolean("BlackWidget", false);
		char[] digits = preferredDigits(context);

		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
				context, 0, new Intent(context, CalendarActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews1 = new RemoteViews(context.getPackageName(),
				R.layout.widget1x1);
		RemoteViews remoteViews4 = new RemoteViews(context.getPackageName(),
				R.layout.widget4x1);
		CivilDate civil = new CivilDate();
		PersianDate persian = DateConverter.civilToPersian(civil);
		persian.setDari(isDariVersion(context));
		int color;
		if (blackWidget) {
			color = context.getResources().getColor(android.R.color.black);
		} else {
			color = context.getResources().getColor(android.R.color.white);
		}
		
		// Widget1x1
		remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
		remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
		remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
				textShaper(persian.getMonthName()));
		remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
				formatNumber(persian.getDayOfMonth(), digits));
		remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1,
				launchAppPendingIntent);
		manager.updateAppWidget(new ComponentName(context,
				CalendarWidget1x1.class), remoteViews1);
		// Widget 4x1
		remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
		remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);

		String text1;
		String text2;
		text1 = getDayOfWeekName(civil.getDayOfWeek());
		text2 = dateToString(persian, digits, true) + PERSIAN_COMMA + " "
				+ dateToString(civil, digits, true);
		if (gadgetClock) {
			text2 = text1 + " " + text2;
			text1 = getPersianFormattedClock(new Date(), digits, gadgetIn24);
		}

		text1 = textShaper(text1);
		text2 = textShaper(text2);

		remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, text1);
		remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1, text2);
		remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1,
				launchAppPendingIntent);
		manager.updateAppWidget(new ComponentName(context,
				CalendarWidget4x1.class), remoteViews4);
		//
	}
}
