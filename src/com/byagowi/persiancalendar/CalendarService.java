package com.byagowi.persiancalendar;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import java.util.Date;

/**
 * The Calendar Service that updates widget time and clock and build/update
 * calendar notification.
 * 
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 */
public class CalendarService extends Service {
	private final CalendarUtils utils = CalendarUtils.getInstance();

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
					update(context);
				}
			}, intentFilter);
		}
		update(getApplicationContext());
		return super.onStartCommand(intent, flags, startId);
	}

	private static final int NOTIFICATION_ID = 1001;
	private NotificationManager mNotificationManager;

	public void update(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		char[] digits = utils.preferredDigits(context);

		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
				context, 0, new Intent(context, CalendarActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		CivilDate civil = new CivilDate();
		PersianDate persian = DateConverter.civilToPersian(civil);
		persian.setDari(utils.isDariVersion(context));

		// Widgets
		{
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			RemoteViews remoteViews1 = new RemoteViews(
					context.getPackageName(), R.layout.widget1x1);
			RemoteViews remoteViews4 = new RemoteViews(
					context.getPackageName(), R.layout.widget4x1);
			int color = prefs.getInt("WidgetTextColor", Color.WHITE);

			// Widget 1x1
			remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
			remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
			remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
					utils.textShaper(persian.getMonthName()));
			remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
					utils.formatNumber(persian.getDayOfMonth(), digits));
			remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1,
					launchAppPendingIntent);
			manager.updateAppWidget(new ComponentName(context,
					CalendarWidget1x1.class), remoteViews1);

			// Widget 4x1
			remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
			remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);

			String text1;
			String text2;
			text1 = utils.getDayOfWeekName(civil.getDayOfWeek());
			String dayTitle = utils.dateToString(persian, digits, true);
			text2 = dayTitle + utils.PERSIAN_COMMA + " "
					+ utils.dateToString(civil, digits, true);

			boolean enableClock = prefs.getBoolean("WidgetClock", true);
			if (enableClock) {
				text2 = text1 + " " + text2;
				boolean in24 = prefs.getBoolean("WidgetIn24", true);
				text1 = utils
						.getPersianFormattedClock(new Date(), digits, in24);
			}

			remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1,
					utils.textShaper(text1));
			remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1,
					utils.textShaper(text2));
			remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1,
					launchAppPendingIntent);
			manager.updateAppWidget(new ComponentName(context,
					CalendarWidget4x1.class), remoteViews4);
		}

		// Notification Permanent Bar
		{
			if (mNotificationManager == null) {
				mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			}
			if (prefs.getBoolean("NotifyDate", true)) {

				String title = utils.getDayOfWeekName(civil.getDayOfWeek())
						+ " " + utils.dateToString(persian, digits, true);

				String contentText = utils.dateToString(civil, digits, true)
						+ utils.PERSIAN_COMMA
						+ " "
						+ utils.dateToString(
								DateConverter.civilToIslamic(civil), digits,
								true);

				Notification notification;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
					notification = notificationBuilderNew(
							launchAppPendingIntent, persian, title, contentText);
				else
					notification = notificationBuilderCompat(
							launchAppPendingIntent, persian, title, contentText);

				mNotificationManager.notify(NOTIFICATION_ID, notification);
			} else {
				mNotificationManager.cancel(NOTIFICATION_ID);
			}
		}
	}

	NotificationCompat.Builder mBuilderCompat;

	private Notification notificationBuilderCompat(
			PendingIntent launchAppPendingIntent, PersianDate persian,
			String title, String contentText) {
		if (mBuilderCompat == null) {
			mBuilderCompat = new NotificationCompat.Builder(this)
					.setPriority(Notification.PRIORITY_LOW)
					.setOngoing(true)
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.launcher_icon));
		}
		return mBuilderCompat
				.setSmallIcon(utils.getDayIconResource(persian.getDayOfMonth()))
				.setContentIntent(launchAppPendingIntent)
				.setContentText(utils.textShaper(contentText))
				.setContentTitle(utils.textShaper(title)).build();
	}

	Notification.Builder mBuilderNew;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private Notification notificationBuilderNew(
			PendingIntent launchAppPendingIntent, PersianDate persian,
			String title, String contentText) {
		if (mBuilderNew == null) {
			mBuilderNew = new Notification.Builder(this)
					.setPriority(Notification.PRIORITY_LOW)
					.setOngoing(true)
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.launcher_icon))
					// Don't show time on notifications, other line must be same
					.setShowWhen(false);
		}
		return mBuilderNew
				.setSmallIcon(utils.getDayIconResource(persian.getDayOfMonth()))
				.setContentIntent(launchAppPendingIntent)
				.setContentText(utils.textShaper(contentText))
				.setContentTitle(utils.textShaper(title)).build();
	}
}
