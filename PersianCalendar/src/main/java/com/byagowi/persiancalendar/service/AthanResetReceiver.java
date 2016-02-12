package com.byagowi.persiancalendar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.byagowi.persiancalendar.Utils;

public class AthanResetReceiver extends BroadcastReceiver {
    private static final String TAG = "AthanResetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AthanResetReceiver received alarm");
        Utils.getInstance().loadAlarms(context);
    }
}
