package com.byagowi.persiancalendar.service;

import android.content.Intent;
import android.content.IntentFilter;

import com.byagowi.persiancalendar.util.UpdateUtils;
import com.byagowi.persiancalendar.util.Utils;

import androidx.work.Worker;

public class UpdateWorker extends Worker {

    private BroadcastReceivers broadcastReceivers;

    @Override
    public Worker.Result doWork() {
        Utils.updateStoredPreference(getApplicationContext());
        UpdateUtils.update(getApplicationContext(), true);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);

        broadcastReceivers = new BroadcastReceivers();
        getApplicationContext().registerReceiver(broadcastReceivers, intentFilter);

        return Result.SUCCESS;
    }

    @Override
    public void onStopped(boolean cancelled) {
        super.onStopped(cancelled);

        getApplicationContext().unregisterReceiver(broadcastReceivers);
    }
}