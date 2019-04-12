package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.content.Context
import android.util.AttributeSet

import androidx.preference.DialogPreference

class AthanVolumePreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    var volume: Int
        get() = getPersistedInt(1)
        set(volume) {
            val wasBlocking = shouldDisableDependents()
            persistInt(volume)
            val isBlocking = shouldDisableDependents()
            if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
        }

    init {
        dialogIcon = null
    }
}
