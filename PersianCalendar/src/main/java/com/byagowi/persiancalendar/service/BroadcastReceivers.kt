package com.byagowi.persiancalendar.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.util.UpdateUtils
import com.byagowi.persiancalendar.util.Utils

/**
 * Startup broadcast receiver
 *
 * @author ebraminio
 */
class BroadcastReceivers : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null || intent == null || intent.action == null) {
      return
    }

    when (intent.action) {
      Intent.ACTION_BOOT_COMPLETED, TelephonyManager.ACTION_PHONE_STATE_CHANGED,
      Constants.BROADCAST_RESTART_APP -> Utils.startEitherServiceOrWorker(context)

      Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
        UpdateUtils.update(context, true)
        Utils.loadApp(context)
      }

      Intent.ACTION_TIME_CHANGED, Intent.ACTION_SCREEN_ON, Constants.BROADCAST_UPDATE_APP -> {
        UpdateUtils.update(context, false)
        Utils.loadApp(context)
      }

      Constants.BROADCAST_ALARM ->
        Utils.startAthan(context, intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY))
    }
  }
}
