package com.byagowi.persiancalendar.ui.utils

import android.content.Context
import android.media.AudioManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.global.isTalkBackEnabled

// a11y related state machine that starts in true state and announce if a transition to true happens
class SensorEventAnnouncer(@StringRes private val text: Int, initialState: Boolean = true) {

    private var state = initialState
    private var lastAnnounce = -1L

    fun check(context: Context, newState: Boolean, isLocked: Boolean = false) {
        if (!isTalkBackEnabled && !isLocked) return
        if (newState && !state) {
            val now = System.currentTimeMillis()
            if (now - lastAnnounce > 2000L) { // 2 seconds
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                // https://stackoverflow.com/a/29423018
                context.getSystemService<AudioManager>()
                    ?.playSoundEffect(AudioManager.FX_KEY_CLICK)
                lastAnnounce = now
            }
        }
        state = newState
    }
}
