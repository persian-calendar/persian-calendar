package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.service.nextDateUpdateMillis
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DateUpdateTests {
    @Test
    fun `next update follows local midnight across both clock changes`() {
        val now = GregorianCalendar(TimeZone.getTimeZone("America/New_York"))
        listOf(
            "2026-03-08T05:00:00Z" to "2026-03-09T04:00:01Z",
            "2026-11-02T04:30:00Z" to "2026-11-02T05:00:01Z",
        ).forEach { (start, expected) ->
            now.timeInMillis = Instant.parse(start).toEpochMilliseconds()
            assertEquals(Instant.parse(expected).toEpochMilliseconds(), nextDateUpdateMillis(now))
        }
    }
}
