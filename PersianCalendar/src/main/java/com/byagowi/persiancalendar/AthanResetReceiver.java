package com.byagowi.persiancalendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AthanResetReceiver extends BroadcastReceiver {
    private static final String TAG = "AthanResetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AthanResetReceiver received alarm");
        Utils.getInstance().loadAlarms(context);
    }
}
