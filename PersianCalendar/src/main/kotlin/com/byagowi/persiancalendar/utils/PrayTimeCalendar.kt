package com.byagowi.persiancalendar.utils

import android.os.Build
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.entities.Clock
import java.util.GregorianCalendar
import java.util.SimpleTimeZone
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal fun GregorianCalendar.prayTimeOffsetMillis(): Int {
    val offset = timeZone.getOffset(timeInMillis)
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
        offset == 4.5.hours.inWholeMilliseconds.toInt() && timeZone.id == IRAN_TIMEZONE_ID
    ) 3.5.hours.inWholeMilliseconds.toInt() else offset
}

internal fun prayTimeInMillis(
    date: GregorianCalendar,
    time: Clock,
    calculationOffsetMillis: Int,
): Long {
    // The engine applies one offset to all clocks. On a transition day that offset may
    // differ from the zone's offset at the event, so interpret the clock in the engine's
    // fixed offset rather than treating it as elapsed time since the zone's midnight.
    val millis = time.toMillis()
    return GregorianCalendar(SimpleTimeZone(calculationOffsetMillis, "PrayTimes")).apply {
        clear()
        set(
            date[GregorianCalendar.YEAR],
            date[GregorianCalendar.MONTH],
            date[GregorianCalendar.DAY_OF_MONTH],
            (millis / 1.hours.inWholeMilliseconds).toInt(),
            (millis / 1.minutes.inWholeMilliseconds % 1.hours.inWholeMinutes).toInt(),
            (millis / 1.seconds.inWholeMilliseconds % 1.minutes.inWholeSeconds).toInt(),
        )
        this[GregorianCalendar.MILLISECOND] = (millis % 1.seconds.inWholeMilliseconds).toInt()
        // Out-of-range hours advance the civil date while retaining the engine's offset
        // and Clock's existing millisecond precision, without changing astronomical values.
    }.timeInMillis
}
