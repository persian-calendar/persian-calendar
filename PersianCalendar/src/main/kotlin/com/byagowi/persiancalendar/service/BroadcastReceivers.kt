package com.byagowi.persiancalendar.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.byagowi.persiancalendar.BROADCAST_ALARM
import com.byagowi.persiancalendar.BROADCAST_RESTART_APP
import com.byagowi.persiancalendar.BROADCAST_UPDATE_APP
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_TIME
import com.byagowi.persiancalendar.Variants.logDebug
import com.byagowi.persiancalendar.utils.scheduleAlarmManagerUpdates
import com.byagowi.persiancalendar.utils.startAthan
import com.byagowi.persiancalendar.utils.startEitherServiceOrWorker
import com.byagowi.persiancalendar.utils.update

/**
 * Startup broadcast receiver
 */
class BroadcastReceivers : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            TelephonyManager.ACTION_PHONE_STATE_CHANGED,
            BROADCAST_RESTART_APP -> startEitherServiceOrWorker(context)

            Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                update(context, true)
                scheduleAlarmManagerUpdates(context)
            }

            Intent.ACTION_TIME_CHANGED, Intent.ACTION_SCREEN_ON, BROADCAST_UPDATE_APP -> {
                update(context, false)
                scheduleAlarmManagerUpdates(context)
            }

            BROADCAST_ALARM -> {
                val key = intent.getStringExtra(KEY_EXTRA_PRAYER) ?: return
                val intendedTime = intent.getLongExtra(KEY_EXTRA_PRAYER_TIME, 0).takeIf { it != 0L }
                logDebug("Alarms: AlarmManager for $key")
                startAthan(context, key, intendedTime)
            }
        }
    }
}
