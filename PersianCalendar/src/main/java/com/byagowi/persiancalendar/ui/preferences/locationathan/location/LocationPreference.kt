package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.utils.getAllCities
import com.byagowi.persiancalendar.utils.language

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

        val city: CityItem? = getAllCities(context, false).firstOrNull { it.key == selected }
        summary = city?.let {
            when (language) {
                LANG_EN_IR, LANG_EN_US, LANG_JA -> it.en
                LANG_CKB -> it.ckb
                LANG_AR -> it.ar
                else -> it.fa
            }
        }
    }
}
