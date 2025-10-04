package com.byagowi.persiancalendar.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.shiftWorkPeriod
import com.byagowi.persiancalendar.global.shiftWorkRecurs
import com.byagowi.persiancalendar.global.shiftWorkStartingJdn
import com.byagowi.persiancalendar.global.shiftWorkTitles
import com.byagowi.persiancalendar.global.shiftWorks
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma

/**
 * Utilities for dealing with repeating shift-work schedules.
 *
 * Improvements and fixes applied:
 *  - Defensive null checks and clearer semantics when shift configuration is missing
 *  - Avoid heavy allocations when not necessary
 *  - Added convenience functions for common UI needs (formatted strings and simple lists)
 */

/**
 * Return the human-readable title for the shift defined on [jdn].
 * If [abbreviated] is true the returned title will be a shortened form (initial letters).
 * Returns null when there is no configured shift for that date.
 */
fun getShiftWorkTitle(jdn: Jdn, abbreviated: Boolean = false): String? {
    val start = shiftWorkStartingJdn ?: return null
    if (shiftWorkPeriod == 0) return null
    if (jdn < start) return null

    val passedDays = jdn - start
    if (!shiftWorkRecurs && passedDays >= shiftWorkPeriod) return null

    val dayInPeriod = (passedDays % shiftWorkPeriod).toInt()

    // Walk the shiftWorks to find which segment this day belongs to
    var accumulation = 0
    var foundType: String? = null
    for (segment in shiftWorks) {
        accumulation += segment.length
        if (accumulation > dayInPeriod) {
            foundType = segment.type
            break
        }
    }
    val type = foundType ?: return null

    // In abbreviated mode skip rest-type entries when configured to do so
    val restKey = "r"
    if (shiftWorkRecurs && abbreviated && (type.equals(restKey, true) || type == shiftWorkTitles[restKey])) return null

    val title = shiftWorkTitles[type] ?: type
    if (!abbreviated) return title

    // produce an abbreviated representation (first letter from each slash-separated part)
    if (title.length <= 2) return title
    return title.split("/")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString("/") { it.substring(0, 1) }
}

/**
 * Compose-friendly helper which summarizes how many days of each shift type appear
 * between tomorrow and the given [jdn]. Returns null if there is nothing interesting to show.
 */
@Composable
fun getShiftWorksInDaysDistance(jdn: Jdn): String? {
    if (shiftWorks.isEmpty()) return null
    val today = Jdn.today()
    val distance = jdn - today
    if (distance !in 1..365) return null

    // Group titles by their occurrences across the date range
    val counts = mutableMapOf<String, Int>()
    var hasNull = false
    var current = today + 1
    while (current <= jdn) {
        val title = getShiftWorkTitle(current)
        if (title == null) hasNull = true else counts[title] = counts.getOrDefault(title, 0) + 1
        current += 1
    }

    if (counts.size < 2 || hasNull) return null

    val parts = counts.entries.map { (title, cnt) ->
        pluralStringResource(R.plurals.days, cnt, formatNumber(cnt)) + " " + title
    }

    return stringResource(R.string.days_distance) + spacedColon + parts.joinToString(spacedComma)
}

/**
 * Returns a list of upcoming shift entries for the next [days] days (date + title).
 * If [days] is non-positive an empty list is returned.
 */
fun getUpcomingShiftWorks(days: Int): List<Pair<Jdn, String?>> {
    if (days <= 0) return emptyList()
    val today = Jdn.today()
    val result = ArrayList<Pair<Jdn, String?>>(days)
    var current = today + 1
    repeat(days) {
        result.add(current to getShiftWorkTitle(current))
        current += 1
    }
    return result
}

/**
 * Count how many times each shift title appears between [start] and [end] (inclusive).
 * Invalid ranges or missing configuration return an empty map.
 */
fun countShiftTypesInRange(start: Jdn, end: Jdn): Map<String, Int> {
    if (shiftWorks.isEmpty() || end < start) return emptyMap()
    val counts = mutableMapOf<String, Int>()
    var current = start
    while (current <= end) {
        val title = getShiftWorkTitle(current)
        if (title != null) counts[title] = counts.getOrDefault(title, 0) + 1
        current += 1
    }
    return counts
}

/**
 * Predicate: whether [jdn] is a rest day. This uses the configured rest key "r" (case-insensitive)
 * and also checks the human-friendly title map in case the rest entry is expressed there.
 */
fun isRestDay(jdn: Jdn): Boolean {
    val title = getShiftWorkTitle(jdn) ?: return false
    val restKey = "r"
    return title.equals(restKey, ignoreCase = true) || title.equals(shiftWorkTitles[restKey], ignoreCase = true)
}

/**
 * Find the next working shift after [start] (scans up to 365 days ahead). Returns the JDN and title,
 * or null if none is found in the probing window.
 */
fun findNextWorkingShift(start: Jdn): Pair<Jdn, String?>? {
    var offset = 1
    while (offset <= 365) {
        val jdn = start + offset
        val title = getShiftWorkTitle(jdn)
        if (title != null && !isRestDay(jdn)) return jdn to title
        offset++
    }
    return null
}

/**
 * Return the full shift schedule for a range [start]..[end] as a list of pairs.
 */
fun listShiftScheduleInRange(start: Jdn, end: Jdn): List<Pair<Jdn, String?>> {
    if (end < start) return emptyList()
    val result = ArrayList<Pair<Jdn, String?>>()
    var current = start
    while (current <= end) {
        result.add(current to getShiftWorkTitle(current))
        current += 1
    }
    return result
}

/** Find all rest days in a given inclusive range. */
fun findRestDaysInRange(start: Jdn, end: Jdn): List<Jdn> {
    if (end < start) return emptyList()
    val restDays = ArrayList<Jdn>()
    var current = start
    while (current <= end) {
        if (isRestDay(current)) restDays.add(current)
        current += 1
    }
    return restDays
}

/**
 * Calculate the percentage ratio of work vs rest days in a closed interval. Returns Pair(workPercent, restPercent).
 */
fun calculateWorkRestRatio(start: Jdn, end: Jdn): Pair<Double, Double> {
    if (end < start) return 0.0 to 0.0
    val totalDays = (end - start + 1).toDouble()
    val restDays = findRestDaysInRange(start, end).size.toDouble()
    val workDays = totalDays - restDays
    return (workDays / totalDays * 100.0) to (restDays / totalDays * 100.0)
}

/**
 * Get the continuous streak (length) of the same shift type starting from [jdn].
 * Returns the title (may be null) and the streak length in days.
 */
fun getShiftStreakFrom(jdn: Jdn): Pair<String?, Int> {
    val title = getShiftWorkTitle(jdn) ?: return null to 0
    var streak = 0
    var current = jdn
    while (getShiftWorkTitle(current) == title) {
        streak++
        current += 1
    }
    return title to streak
}
 
