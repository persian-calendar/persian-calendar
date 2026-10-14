package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.utils.debugAssertNotNull
import io.github.persiancalendar.calendar.CivilDate
import java.util.GregorianCalendar
import java.util.TimeZone

/** One provider occurrence. All-day endpoints are floating dates encoded at UTC midnight. */
class DeviceEventInterval(
    val beginMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean,
    displayTimeZone: TimeZone,
) {
    private val timeZone = if (isAllDay) TimeZone.getTimeZone("UTC") else
        (displayTimeZone.clone() as? TimeZone).debugAssertNotNull ?: displayTimeZone
    val start: GregorianCalendar get() = calendarAt(beginMillis)
    val end: GregorianCalendar get() = calendarAt(endMillis)
    val date: CivilDate
        get() = start.let {
            CivilDate(it[GregorianCalendar.YEAR], it[GregorianCalendar.MONTH] + 1, it[GregorianCalendar.DAY_OF_MONTH])
        }

    private fun calendarAt(millis: Long) = GregorianCalendar(timeZone).apply { timeInMillis = millis }

    private fun dayBounds(date: CivilDate): Pair<Long, Long> {
        val start = startOfDay(date, timeZone)
        val end = startOfDay(CivilDate(date.toJdn() + 1), timeZone)
        return start.timeInMillis to end.timeInMillis
    }

    fun occupies(date: CivilDate): Boolean {
        val (dayStart, dayEnd) = dayBounds(date)
        if (dayStart >= dayEnd) return false
        // Preserve point events and records with missing/invalid ends on their start day.
        if (endMillis <= beginMillis) return beginMillis >= dayStart && beginMillis < dayEnd
        return beginMillis < dayEnd && endMillis > dayStart
    }

    /** A timed occurrence clipped to a local calendar day, for the 0..24 wall-clock grid. */
    fun clipToDay(date: CivilDate): DeviceEventDaySegment? {
        if (isAllDay || !occupies(date)) return null
        val (dayStart, dayEnd) = dayBounds(date)
        val clippedStart = maxOf(beginMillis, dayStart)
        val clippedEnd = minOf(maxOf(beginMillis, endMillis), dayEnd)
        val startHour = hourAt(clippedStart)
        val wallEndHour = if (clippedEnd == dayEnd) 24f else hourAt(clippedEnd)
        // The fixed wall-clock grid has one row for the repeated DST hour. Keep an occurrence
        // crossing that backward transition visible with its elapsed span when clocks reverse.
        val endHour = if (wallEndHour <= startHour && clippedEnd > clippedStart) {
            (startHour + (clippedEnd - clippedStart) / 3_600_000f).coerceAtMost(24f)
        } else wallEndHour
        return DeviceEventDaySegment(clippedStart, clippedEnd, startHour, endHour)
    }

    private fun hourAt(millis: Long): Float = calendarAt(millis).let {
        it[GregorianCalendar.HOUR_OF_DAY] + it[GregorianCalendar.MINUTE] / 60f +
                it[GregorianCalendar.SECOND] / 3600f + it[GregorianCalendar.MILLISECOND] / 3_600_000f
    }

    companion object {
        /** Earliest valid instant of a civil date; skipped dates resolve to the following day. */
        fun startOfDay(date: CivilDate, timeZone: TimeZone): GregorianCalendar {
            val start = GregorianCalendar(timeZone).apply {
                clear()
                set(date.year, date.month - 1, date.dayOfMonth)
            }
            val wallMidnight = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(date.year, date.month - 1, date.dayOfMonth)
            }.timeInMillis
            var resolved = start.timeInMillis

            val previousOffset = timeZone.getOffset(resolved - 86_400_000L)
            val earlier = wallMidnight - previousOffset
            if (earlier < resolved && timeZone.getOffset(earlier) == previousOffset) resolved = earlier

            if (resolved + timeZone.getOffset(resolved) > wallMidnight) {
                var before = wallMidnight - timeZone.getOffset(resolved)
                var after = resolved
                while (before + 1 < after) {
                    val middle = before + (after - before) / 2
                    if (middle + timeZone.getOffset(middle) >= wallMidnight) after = middle
                    else before = middle
                }
                resolved = after
            }
            return start.apply { timeInMillis = resolved }
        }
    }
}

data class DeviceEventDaySegment(
    val startMillis: Long, val endMillis: Long, val startHour: Float, val endHour: Float,
)
