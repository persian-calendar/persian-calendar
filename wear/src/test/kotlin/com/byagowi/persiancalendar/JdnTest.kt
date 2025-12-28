package com.byagowi.persiancalendar

import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JdnTest {
    @Test
    fun `test Jdn creation from CivilDate`() {
        val civilDate = CivilDate(2025, 12, 18)
        val jdn = Jdn(civilDate)
        assertEquals(civilDate, jdn.toCivilDate())
    }

    @Test
    fun `test Jdn creation from PersianDate`() {
        val persianDate = PersianDate(1403, 9, 27)
        val jdn = Jdn(persianDate)
        assertEquals(persianDate, jdn.toPersianDate())
    }

    @Test
    fun `test Jdn addition`() {
        val jdn = Jdn(CivilDate(2025, 12, 18))
        val nextDay = jdn + 1
        assertEquals(CivilDate(2025, 12, 19), nextDay.toCivilDate())
    }

    @Test
    fun `test Jdn subtraction`() {
        val jdn = Jdn(CivilDate(2025, 12, 18))
        val previousDay = jdn - 1
        assertEquals(CivilDate(2025, 12, 17), previousDay.toCivilDate())
    }

    @Test
    fun `test Jdn difference`() {
        val jdn1 = Jdn(CivilDate(2025, 12, 18))
        val jdn2 = Jdn(CivilDate(2025, 12, 25))
        assertEquals(7, jdn2 - jdn1)
    }

    @Test
    fun `test Jdn comparison`() {
        val jdn1 = Jdn(CivilDate(2025, 12, 18))
        val jdn2 = Jdn(CivilDate(2025, 12, 25))
        assertTrue(jdn1 < jdn2)
        assertTrue(jdn2 > jdn1)
    }

    @Test
    fun `test Jdn rangeTo creates correct sequence`() {
        val start = Jdn(CivilDate(2025, 12, 18))
        val end = Jdn(CivilDate(2025, 12, 20))
        val range = (start..end).toList()

        assertEquals(3, range.size)
        assertEquals(start, range[0])
        assertEquals(start + 1, range[1])
        assertEquals(end, range[2])
    }

    @Test
    fun `test Jdn rangeUntil excludes end`() {
        val start = Jdn(CivilDate(2025, 12, 18))
        val end = Jdn(CivilDate(2025, 12, 20))
        val range = (start..<end).toList()

        assertEquals(2, range.size)
        assertEquals(start, range[0])
        assertEquals(start + 1, range[1])
    }

    @Test
    fun `test conversion between calendar systems`() {
        val persianDate = PersianDate(1403, 9, 27)
        val jdn = Jdn(persianDate)

        // Round-trip
        assertEquals(persianDate, jdn.toPersianDate())

        val civilDate = jdn.toCivilDate()
        val islamicDate = jdn.toIslamicDate()

        assertEquals(jdn, Jdn(civilDate))
        assertEquals(jdn, Jdn(islamicDate))
    }

    @Test
    fun `test today returns valid Jdn`() {
        val today = Jdn.today()
        val civilDate = today.toCivilDate()

        assertTrue(civilDate.year in 2000..2100)
        assertTrue(civilDate.month in 1..12)
        assertTrue(civilDate.dayOfMonth in 1..31)
    }
}
