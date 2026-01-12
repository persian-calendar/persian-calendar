package com.byagowi.persiancalendar.utils

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_RECURS
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_SETTING
import com.byagowi.persiancalendar.PREF_SHIFT_WORK_STARTING_JDN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.ShiftWorkRecord
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.shiftWorkTitles
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma

data class ShiftWorkSettings(
    val recurs: Boolean,
    val startingJdn: Jdn?,
    val records: List<ShiftWorkRecord>,
) {
    constructor(preferences: SharedPreferences) : this(
        recurs = preferences.getBoolean(PREF_SHIFT_WORK_RECURS, true),
        startingJdn = preferences.getJdnOrNull(PREF_SHIFT_WORK_STARTING_JDN),
        records = (preferences.getString(PREF_SHIFT_WORK_SETTING, null)
            .orEmpty()).splitFilterNotEmpty(",").map { it.splitFilterNotEmpty("=") }
            .filter { it.size == 2 }.map { ShiftWorkRecord(it[0], it[1].toIntOrNull() ?: 1) },
    )

    private val totalDays: Int = records.sumOf { it.length }

    fun workTitle(jdn: Jdn, abbreviated: Boolean = false): String? {
        val shiftWorkStartingJdn = startingJdn ?: return null
        if (jdn < shiftWorkStartingJdn || totalDays == 0) return null

        val passedDays = jdn - shiftWorkStartingJdn
        if (!recurs && passedDays >= totalDays) return null

        val dayInPeriod = passedDays % totalDays

        var accumulation = 0
        val type = records.firstOrNull {
            accumulation += it.length
            accumulation > dayInPeriod
        }?.type ?: return null

        // Skip rests on abbreviated mode
        if (recurs && abbreviated && (type == "r" || type == shiftWorkTitles["r"])) return null

        val title = shiftWorkTitles[type] ?: type
        return if (abbreviated && title.isNotEmpty() && title.length > 2) {
            title.split("/").map { it.trim() }.filter { it.isNotEmpty() }
                .joinToString("/") { it.firstOrNull()?.toString().orEmpty() }
        } else title
    }

    @Composable
    fun getShiftWorksInDaysDistance(jdn: Jdn): String? {
        if (records.isEmpty()) return null
        val today = Jdn.today()
        if ((jdn - today) !in 1..365) return null
        val shiftWorksInDaysDistance = (today + 1..jdn).groupBy(::workTitle)
        if (shiftWorksInDaysDistance.size < 2 || null in shiftWorksInDaysDistance) return null
        @Suppress("SimplifiableCallChain") return stringResource(R.string.days_distance) + spacedColon + shiftWorksInDaysDistance.entries.map { (title, days) ->
            pluralStringResource(
                R.plurals.days, days.size, numeral.format(days.size),
            ) + " " + title
        }.joinToString(spacedComma)
    }

    companion object {
        val empty = ShiftWorkSettings(true, null, emptyList())
    }
}
