package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.LunarAge
import com.byagowi.persiancalendar.entities.Zodiac
import com.byagowi.persiancalendar.utils.Eclipse
import com.cepmuvakkit.times.posAlgo.Ecliptic
import com.google.common.truth.Truth.assertThat
import io.github.persiancalendar.calendar.PersianDate
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class AstronomyTests {

    @Test
    fun `Test lunar age`() {
        // https://github.com/BGCX262/zweer-gdr-svn-to-git/blob/6d85903/trunk/library/Zwe/Weather/Moon.php
        listOf(
            1.84566, 5.53699, 9.22831, 12.91963, 16.61096, 20.30228, 23.99361, 27.68493
        ).forEachIndexed { index, it ->
            val lunarAge = LunarAge.fromDegrees((index + .5) * (360 / 8))
            assertThat(lunarAge.days).isWithin(1.0e-5).of(it)
            assertThat(lunarAge.toPhase().ordinal).isEqualTo((index + 1) % 8)
        }
        (0..720).map { LunarAge.fromDegrees(it.toDouble()).tithi }.forEach {
            assertThat(it).isAtLeast(1)
            assertThat(it).isAtMost(30)
        }
    }

    @Test
    fun `Basic Eclipse testing`() {
        val nextSolarEclipse =
            Eclipse(GregorianCalendar(2021, 12, 30), Eclipse.Category.SOLAR, true)
        assertThat(nextSolarEclipse.maxPhaseDate.time).isEqualTo(1651351538312)
        assertThat(nextSolarEclipse.gamma).isWithin(1.0e-10).of(-1.1873364080976627)
        assertThat(nextSolarEclipse.phase).isWithin(1.0e-10).of(.6437521307583453)
        assertThat(nextSolarEclipse.u).isWithin(1.0e-10).of(.014993006616545197)

        val nextLunarEclipse =
            Eclipse(GregorianCalendar(2021, 12, 30), Eclipse.Category.LUNAR, true)
        assertThat(nextLunarEclipse.maxPhaseDate.time).isEqualTo(1652674277164)
        assertThat(nextLunarEclipse.gamma).isWithin(1.0e-10).of(-.25690754557964146)
        assertThat(nextLunarEclipse.phase).isWithin(1.0e-10).of(1.4071957485281614)
        assertThat(nextLunarEclipse.u).isWithin(1.0e-10).of(-.010929228527489551)

        val prevSolarEclipse =
            Eclipse(GregorianCalendar(2021, 12, 30), Eclipse.Category.SOLAR, false)
        assertThat(prevSolarEclipse.maxPhaseDate.time).isEqualTo(1638603148198)
        assertThat(prevSolarEclipse.gamma).isWithin(1.0e-10).of(-.9551661328959204)
        assertThat(prevSolarEclipse.phase).isWithin(1.0e-10).of(1.0)
        assertThat(prevSolarEclipse.u).isWithin(1.0e-10).of(-.0083259217061097)

        val prevLunarEclipse =
            Eclipse(GregorianCalendar(2021, 12, 30), Eclipse.Category.LUNAR, false)
        assertThat(prevLunarEclipse.maxPhaseDate.time).isEqualTo(1637312710047)
        assertThat(prevLunarEclipse.gamma).isWithin(1.0e-10).of(-.45412353634065095)
        assertThat(prevLunarEclipse.phase).isWithin(1.0e-10).of(.975060595761878)
        assertThat(prevLunarEclipse.u).isWithin(1.0e-10).of(.027368438969125428)
    }

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
            assertEquals(zodiac, Zodiac.fromIauEcliptic(Ecliptic(longitude.toDouble(), .0, .0)))
        }
        (0..11).map { 10 + it * 30 }
            .zip(Zodiac.values() + listOf(Zodiac.ARIES)) { longitude, zodiac ->
                assertEquals(
                    zodiac,
                    Zodiac.fromTropicalEcliptic(Ecliptic(longitude.toDouble(), .0, .0))
                )
            }
    }

    @Test
    fun `Zodiac center of range`() {
        listOf(
            10.88, 42.17, 72.3, 106.46, 127.39, 154.32,
            198.755, 233.37, 256.915, 286.875, 307.105, 330.15
        ).zip(Zodiac.values()) { centerOfZodiac, zodiac ->
            val average = zodiac.iauRange.sum() / 2
            assertThat(average).isWithin(1.0e-10).of(centerOfZodiac)
        }
        (0..11).zip(Zodiac.values()) { it, zodiac ->
            val average = zodiac.tropicalRange.sum() / 2
            assertThat(average).isWithin(1.0e-10).of(it * 30.0 + 15)
        }
    }
}
