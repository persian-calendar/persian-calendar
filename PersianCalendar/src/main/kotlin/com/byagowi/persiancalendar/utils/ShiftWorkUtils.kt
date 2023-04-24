package com.byagowi.persiancalendar.utils

import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.shiftWorkPeriod
import com.byagowi.persiancalendar.global.shiftWorkRecurs
import com.byagowi.persiancalendar.global.shiftWorkStartingJdn
import com.byagowi.persiancalendar.global.shiftWorkTitles
import com.byagowi.persiancalendar.global.shiftWorks

fun getShiftWorkTitle(jdn: Jdn, abbreviated: Boolean): String {
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
