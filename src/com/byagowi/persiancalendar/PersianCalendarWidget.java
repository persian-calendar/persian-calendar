package com.byagowi.persiancalendar;

import java.util.Date;

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
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Program widget on Android launcher.
 * 
 * @author ebraminio
 * 
 */
public class PersianCalendarWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		try {

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget);

			CivilDate civil = new CivilDate();
			PersianDate persian = DateConverter.civilToPersian(civil);

			remoteViews.setTextViewText(R.id.clockText, PersianUtils.RLM
					+ PersianUtils.getPersianFormattedClock(new Date()));

			remoteViews.setTextViewText(R.id.calendarText, PersianUtils.RLM
					+ PersianUtils.getDayOfWeekName(civil.getDayOfWeek())
					+ PersianUtils.PERSIAN_COMMA + " " + persian.toString());

			Intent launchAppIntent = new Intent(context,
					PersianCalendarActivity.class);
			PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
					context, 0, launchAppIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widget_layout,
					launchAppPendingIntent);

			ComponentName widget = new ComponentName(context,
					PersianCalendarWidget.class);
			appWidgetManager.updateAppWidget(widget, remoteViews);
		} catch (Exception e) {
			Log.e("com.byagowi.persiancalendar", e.toString());
		}
	}
}
