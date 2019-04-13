package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.content.Context
import android.util.AttributeSet
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
    }
}
