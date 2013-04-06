package com.byagowi.persiancalendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import java.util.Calendar;
import java.util.Date;

/**
 * The Calendar Service that updates widget time and clock and build/update
 * calendar notification.
 * 
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 */
public class ApplicationService extends Service {
	private final UpdateUtils updateUtils = UpdateUtils.getInstance();

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	private static int count = 0;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		count++;
		if (count == 1) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
			intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
			intentFilter.addAction(Intent.ACTION_SCREEN_ON);
			intentFilter.addAction(Intent.ACTION_TIME_TICK);
			registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateUtils.update(context);
				}
			}, intentFilter);
		}
		updateUtils.update(getApplicationContext());
		return super.onStartCommand(intent, flags, startId);
	}
}
