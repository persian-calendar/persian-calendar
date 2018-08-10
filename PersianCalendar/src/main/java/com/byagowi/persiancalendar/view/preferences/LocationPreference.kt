package com.byagowi.persiancalendar.view.preferences

import android.content.Context
import android.content.Intent
import android.util.AttributeSet

import com.byagowi.persiancalendar.Constants

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.DialogPreference

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
class LocationPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

  fun setSelected(selected: String) {
    val wasBlocking = shouldDisableDependents()
    persistString(selected)
    val isBlocking = shouldDisableDependents()
    if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
    LocalBroadcastManager.getInstance(context)
        .sendBroadcast(Intent(Constants.LOCAL_INTENT_UPDATE_PREFERENCE))
  }
}
