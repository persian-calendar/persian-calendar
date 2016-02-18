package com.byagowi.persiancalendar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.byagowi.persiancalendar.Utils;

/**
 * Startup broadcast receiver
 *
 * @author ebraminio
 */
public class SystemStartup extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ApplicationService.class));
        Utils.getInstance(context).setAthanRepeater();
    }
}
