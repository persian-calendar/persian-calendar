package com.byagowi.persiancalendar.ui.athan;

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.utils.logException

class PreventPhoneCallIntervention(private val onCallDetect: () -> Unit) {
    var stopListener = {}
        private set

    private fun onStateChange(state: Int) {
        if (state == TelephonyManager.CALL_STATE_RINGING ||
            state == TelephonyManager.CALL_STATE_OFFHOOK
        ) {
            stopListener()
            onCallDetect()
        }
    }

    fun startListener(context: Context) {
        val telephonyManager = runCatching { context.getSystemService<TelephonyManager>() }
            .onFailure(logException).getOrNull() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val listener = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) = onStateChange(state)
            }
            runCatching {
                telephonyManager.registerTelephonyCallback(context.mainExecutor, listener)
                stopListener = {
                    runCatching { telephonyManager.unregisterTelephonyCallback(listener) }
                        .onFailure(logException)
                    stopListener = {}
                }
            }.onFailure(logException).onFailure { startFallbackListener(telephonyManager) }
        } else startFallbackListener(telephonyManager)
    }

    @Suppress("DEPRECATION")
    private fun startFallbackListener(telephonyManager: TelephonyManager) {
        val listener = object : PhoneStateListener() {
            @Deprecated("", ReplaceWith(""))
            override fun onCallStateChanged(state: Int, incomingNumber: String) =
                onStateChange(state)
        }
        runCatching { telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE) }
            .onFailure(logException)
        stopListener = {
            runCatching { telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE) }
                .onFailure(logException)
            stopListener = {}
        }
    }
}
