package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Zodiac
import com.cepmuvakkit.times.posAlgo.Ecliptic
import com.google.common.truth.Truth.assertThat
import io.github.persiancalendar.calendar.PersianDate
import junit.framework.Assert.assertEquals
import org.junit.Test

class ZodiacTests {

    @Test
    fun `Zodiac from Persian calendar`() {
        (1..12).forEach {
            assertEquals(
                Zodiac.values()[it - 1],
                Zodiac.fromPersianCalendar(PersianDate(1400, it, 1))
            )
        }
        assertEquals(Zodiac.LEO, Zodiac.fromPersianCalendar(PersianDate(1400, 5, 1)))
    }

    @Test
    fun `Zodiac from ecliptic`() {
        listOf(10, 40, 60, 100, 130, 140, 180, 230, 260, 280, 310, 320, 350).zip(
            Zodiac.values() + listOf(Zodiac.ARIES)
        ) { longitude, zodiac ->
            assertEquals(zodiac, Zodiac.fromEcliptic(Ecliptic(longitude.toDouble(), .0, .0)))
        }
    }

    @Test
    fun `Zodiac center of range`() {
        listOf(
            10.88, 42.17, 72.3, 106.46, 127.39, 154.32,
            198.755, 233.37, 256.915, 286.875, 307.105, 330.15
        ).zip(Zodiac.values()) { centerOfZodiac, zodiac ->
            assertThat(zodiac.centerOfRange).isWithin(1.0e-10).of(centerOfZodiac)
        }
    }
}
