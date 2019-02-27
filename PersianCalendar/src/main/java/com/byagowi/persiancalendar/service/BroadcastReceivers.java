package com.byagowi.persiancalendar.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.utils.UpdateUtils;
import com.byagowi.persiancalendar.utils.Utils;

/**
 * Startup broadcast receiver
 *
 * @author ebraminio
 */
public class BroadcastReceivers extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null || intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
            case Constants.BROADCAST_RESTART_APP:
                Utils.startEitherServiceOrWorker(context);
                break;

            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
                UpdateUtils.update(context, true);
                Utils.loadApp(context);
                break;

            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_SCREEN_ON:
            case Constants.BROADCAST_UPDATE_APP:
                UpdateUtils.update(context, false);
                Utils.loadApp(context);
                break;

            case Constants.BROADCAST_ALARM:
                Utils.startAthan(context, intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY));
                break;
        }
    }
}
