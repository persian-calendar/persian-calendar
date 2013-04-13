package com.byagowi.persiancalendar.daemon;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Startup broadcast receiver
 * 
 * @author ebraminio
 */
public class StartupListener extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, Daemon.class));
	}
}
