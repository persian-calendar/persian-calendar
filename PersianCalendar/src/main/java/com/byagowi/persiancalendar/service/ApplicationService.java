package com.byagowi.persiancalendar.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.util.UpdateUtils;

/**
 * The Calendar Service that updates widget time and clock and build/update
 * calendar notification.
 *
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 */
public class ApplicationService extends Service {
    private UpdateUtils updateUtils;
    private boolean first = true;

    @Override
    public IBinder onBind(Intent paramIntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateUtils = UpdateUtils.getInstance(getApplicationContext());
        if (first) {
            first = false;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_TIME_TICK);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateUtils.update();
                }
            }, intentFilter);
        }

        Utils utils = Utils.getInstance(getBaseContext());
        utils.loadApp();
        updateUtils.update();
        utils.loadAlarms();
        return START_STICKY;
    }
}
