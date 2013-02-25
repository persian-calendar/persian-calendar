package com.byagowi.persiancalendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import java.util.Date;

/**
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
	private NotificationCompat.Builder mNotifyBuilder;

	public void update(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean gadgetClock = prefs.getBoolean("GadgetClock", true);
		boolean gadgetIn24 = prefs.getBoolean("GadgetIn24", false);
		boolean blackWidget = prefs.getBoolean("BlackWidget", false);
		char[] digits = utils.preferredDigits(context);

		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
				context, 0, new Intent(context, CalendarActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews1 = new RemoteViews(context.getPackageName(),
				R.layout.widget1x1);
		RemoteViews remoteViews4 = new RemoteViews(context.getPackageName(),
				R.layout.widget4x1);
		CivilDate civil = new CivilDate();
		PersianDate persian = DateConverter.civilToPersian(civil);
		persian.setDari(utils.isDariVersion(context));

		// Widget1x1
		{
			int color = context.getResources()
					.getColor(
							blackWidget ? android.R.color.black
									: android.R.color.white);

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
			if (gadgetClock) {
				text2 = text1 + " " + text2;
				text1 = utils.getPersianFormattedClock(new Date(), digits,
						gadgetIn24);
			}

			text1 = utils.textShaper(text1);
			text2 = utils.textShaper(text2);

			remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1,
					utils.textShaper(text1));
			remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1,
					utils.textShaper(text2));
			remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1,
					launchAppPendingIntent);
			manager.updateAppWidget(new ComponentName(context,
					CalendarWidget4x1.class), remoteViews4);
		}
		//

		// notification
		{
			if (mNotificationManager == null) {
				mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			}
			if (prefs.getBoolean("NotifyDate", true)) {
				if (mNotifyBuilder == null) {
					mNotifyBuilder = new NotificationCompat.Builder(this)
							.setLargeIcon(
									BitmapFactory.decodeResource(
											getResources(),
											R.drawable.launcher_icon))
							.setPriority(Notification.PRIORITY_LOW)
							.setContentIntent(launchAppPendingIntent)
							.setOngoing(true);
				}

				String title = utils.getDayOfWeekName(civil.getDayOfWeek())
						+ " "
						+ utils.dateToString(persian, digits, true);
				
				String contentText = utils.dateToString(civil, digits, true)
						+ utils.PERSIAN_COMMA
						+ " "
						+ utils.dateToString(
								DateConverter.civilToIslamic(civil), digits,
								true);

				mNotifyBuilder
						.setSmallIcon(
								utils.getDayIconResource(persian
										.getDayOfMonth()))
						.setContentText(utils.textShaper(contentText))
						.setContentTitle(utils.textShaper(title));

				mNotificationManager.notify(NOTIFICATION_ID,
						mNotifyBuilder.build());
			} else {
				mNotificationManager.cancel(NOTIFICATION_ID);
			}
		}
		//
	}
}
