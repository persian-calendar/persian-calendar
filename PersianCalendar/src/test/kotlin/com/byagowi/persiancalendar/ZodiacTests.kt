package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Zodiac
import com.cepmuvakkit.times.posAlgo.Ecliptic
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
        assertEquals(Zodiac.ARIES, Zodiac.fromEcliptic(Ecliptic(10.0, .0, .0)))
        assertEquals(Zodiac.TAURUS, Zodiac.fromEcliptic(Ecliptic(40.0, .0, .0)))
        assertEquals(Zodiac.GEMINI, Zodiac.fromEcliptic(Ecliptic(60.0, .0, .0)))
        assertEquals(Zodiac.CANCER, Zodiac.fromEcliptic(Ecliptic(100.0, .0, .0)))
        assertEquals(Zodiac.LEO, Zodiac.fromEcliptic(Ecliptic(130.0, .0, .0)))
        assertEquals(Zodiac.VIRGO, Zodiac.fromEcliptic(Ecliptic(140.0, .0, .0)))
        assertEquals(Zodiac.LIBRA, Zodiac.fromEcliptic(Ecliptic(180.0, .0, .0)))
        assertEquals(Zodiac.SCORPIO, Zodiac.fromEcliptic(Ecliptic(230.0, .0, .0)))
        assertEquals(Zodiac.SAGITTARIUS, Zodiac.fromEcliptic(Ecliptic(260.0, .0, .0)))
        assertEquals(Zodiac.CAPRICORN, Zodiac.fromEcliptic(Ecliptic(280.0, .0, .0)))
        assertEquals(Zodiac.AQUARIUS, Zodiac.fromEcliptic(Ecliptic(310.0, .0, .0)))
        assertEquals(Zodiac.PISCES, Zodiac.fromEcliptic(Ecliptic(320.0, .0, .0)))
        assertEquals(Zodiac.ARIES, Zodiac.fromEcliptic(Ecliptic(350.0, .0, .0)))
    }
}
