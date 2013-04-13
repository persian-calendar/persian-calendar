package com.byagowi.persiancalendar.daemon;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * The Calendar Service that updates widget time and clock and build/update
 * calendar notification.
 * 
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 */
public class Daemon extends Service {
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
