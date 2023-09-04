package com.byagowi.persiancalendar.utils

import android.content.Context
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.shiftWorkPeriod
import com.byagowi.persiancalendar.global.shiftWorkRecurs
import com.byagowi.persiancalendar.global.shiftWorkStartingJdn
import com.byagowi.persiancalendar.global.shiftWorkTitles
import com.byagowi.persiancalendar.global.shiftWorks
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma

fun getShiftWorkTitle(jdn: Jdn, abbreviated: Boolean = false): String {
    val shiftWorkStartingJdn = shiftWorkStartingJdn ?: return ""
    if (jdn < shiftWorkStartingJdn || shiftWorkPeriod == 0)
        return ""

    val passedDays = jdn - shiftWorkStartingJdn
    if (!shiftWorkRecurs && passedDays >= shiftWorkPeriod) return ""

    val dayInPeriod = passedDays % shiftWorkPeriod

    var accumulation = 0
    val type = shiftWorks.firstOrNull {
        accumulation += it.length
        accumulation > dayInPeriod
    }?.type ?: return ""

    // Skip rests on abbreviated mode
    if (shiftWorkRecurs && abbreviated && (type == "r" || type == shiftWorkTitles["r"])) return ""

    val title = shiftWorkTitles[type] ?: type
    return if (abbreviated && title.isNotEmpty())
        title.split("/").map { it.trim() }.filter { it.isNotEmpty() }
            .joinToString("/") { it.substring(0, 1) }
    else title
}

fun getShiftWorksInDaysDistance(jdn: Jdn, context: Context): String? {
    val shiftWorkStartingJdn = shiftWorkStartingJdn ?: return null
    if (shiftWorks.isEmpty()) return null
    val today = Jdn.today()
    if ((jdn - today) !in 1..365 || shiftWorkStartingJdn > today) return null
    val shiftWorksInDaysDistance = (today + 1..jdn).groupBy(::getShiftWorkTitle)
    if (shiftWorksInDaysDistance.size < 2) return null
    return context.getString(R.string.days_distance) + spacedColon +
            shiftWorksInDaysDistance.entries.joinToString(spacedComma) { (title, days) ->
                context.resources.getQuantityString(
                    R.plurals.n_days, days.size, formatNumber(days.size)
                ) + " " + title
            }
}
