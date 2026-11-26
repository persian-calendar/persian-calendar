package com.byagowi.persiancalendar

import io.github.persiancalendar.calendar.CivilDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventsTest {
    @Test
    fun `programmers day follows leap years and the international event setting`() {
        fun hasEvent(date: CivilDate, enabled: Set<String>) =
            getEventsOfDay(enabled, date).any { "برنامه‌نویس" in it.title }

        val enabled = setOf(internationalKey)
        assertTrue(hasEvent(CivilDate(2024, 9, 12), enabled))
        assertTrue(hasEvent(CivilDate(2025, 9, 13), enabled))
        assertFalse(hasEvent(CivilDate(2025, 9, 12), enabled))
        assertFalse(hasEvent(CivilDate(2025, 9, 13), emptySet()))
    }
}
