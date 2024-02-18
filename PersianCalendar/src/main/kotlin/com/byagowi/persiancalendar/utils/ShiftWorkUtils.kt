package com.byagowi.persiancalendar.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.shiftWorkPeriod
import com.byagowi.persiancalendar.global.shiftWorkRecurs
import com.byagowi.persiancalendar.global.shiftWorkStartingJdn
import com.byagowi.persiancalendar.global.shiftWorkTitles
import com.byagowi.persiancalendar.global.shiftWorks
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.utils.stringResource

fun getShiftWorkTitle(jdn: Jdn, abbreviated: Boolean = false): String? {
    val shiftWorkStartingJdn = shiftWorkStartingJdn ?: return null
    if (jdn < shiftWorkStartingJdn || shiftWorkPeriod == 0) return null

    val passedDays = jdn - shiftWorkStartingJdn
    if (!shiftWorkRecurs && passedDays >= shiftWorkPeriod) return null

    val dayInPeriod = passedDays % shiftWorkPeriod

    var accumulation = 0
    val type = shiftWorks.firstOrNull {
        accumulation += it.length
        accumulation > dayInPeriod
    }?.type ?: return null

    // Skip rests on abbreviated mode
    if (shiftWorkRecurs && abbreviated && (type == "r" || type == shiftWorkTitles["r"])) return null

    val title = shiftWorkTitles[type] ?: type
    return if (abbreviated && title.isNotEmpty()) title.split("/").map { it.trim() }
        .filter { it.isNotEmpty() }.joinToString("/") { it.substring(0, 1) }
    else title
}

@Composable
fun getShiftWorksInDaysDistance(jdn: Jdn): String? {
    if (shiftWorks.isEmpty()) return null
    val today = Jdn.today()
    if ((jdn - today) !in 1..365) return null
    val shiftWorksInDaysDistance = (today + 1..jdn).groupBy(::getShiftWorkTitle)
    if (shiftWorksInDaysDistance.size < 2 || null in shiftWorksInDaysDistance) return null
    return stringResource(R.string.days_distance) + spacedColon + shiftWorksInDaysDistance.entries.map { (title, days) ->
        pluralStringResource(
            R.plurals.n_days, days.size, formatNumber(days.size)
        ) + " " + title
    }.joinToString(spacedComma)
}
