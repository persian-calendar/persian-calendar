package com.byagowi.persiancalendar.ui.athan;

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.utils.logException

class PreventPhoneCallIntervention(callback: () -> Unit) {
    fun start(context: Context) {
        runCatching {
            context.getSystemService<TelephonyManager>()
                ?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }.onFailure(logException)
    }

    fun stop(context: Context) {
        runCatching {
            context.getSystemService<TelephonyManager>()
                ?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
            phoneStateListener = null
        }.onFailure(logException)
    }

    private var phoneStateListener: PhoneStateListener? = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                callback()
            }
        }
    }
}
