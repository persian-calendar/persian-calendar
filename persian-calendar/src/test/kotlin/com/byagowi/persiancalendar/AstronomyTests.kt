package com.byagowi.persiancalendar

import android.icu.util.ChineseCalendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.LunarAge
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.google.common.truth.Truth.assertThat
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.test.assertEquals

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
        val time = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        time.clear()
        time.set(2022, 1, 13, 0, 0, 0)
//        assertThat($(time, null, 0.0).lunarAge.days).isWithin(1.0e-2).of(11.31)
//        assertThat($(time, null, 0.0).lunarAge.tithi).isEqualTo(12)
//        assertThat($(time, null, 0.0).lunarSunlitTilt).isWithin(1.0e0).of(180.0)

//        val kathmandu = Coordinates(27.7172, 85.324, 1_400.0)
//        assertThat($(time, kathmandu, 0.0).lunarAge.days).isWithin(1.0e-2).of(11.31)
//        assertThat($(time, kathmandu, 0.0).lunarAge.tithi).isEqualTo(12)
//        assertThat($(time, kathmandu, 0.0).lunarSunlitTilt).isWithin(1.0e0).of(180.0)

//        val nirobi = Coordinates(-1.286389, 36.817222, 1_795.0)
//        assertThat($(time, nirobi, 0.0).lunarSunlitTilt).isWithin(1.0e0).of(.0)
    }

    @Test
    fun `Zodiac from Persian calendar`() {
        (1..12).forEach {
            assertEquals(
                Zodiac.entries[it - 1],
                Zodiac.fromPersianCalendar(PersianDate(1400, it, 1))
            )
        }
        assertEquals(Zodiac.LEO, Zodiac.fromPersianCalendar(PersianDate(1400, 5, 1)))
    }

    @Test
    fun `Zodiac from ecliptic`() {
        listOf(10, 40, 60, 100, 130, 140, 180, 230, 260, 280, 310, 320, 350).zip(
            Zodiac.entries + Zodiac.ARIES
        ) { longitude, zodiac ->
            assertEquals(zodiac, Zodiac.fromIau(longitude.toDouble()))
        }
        (0..11).map { 20 + it * 30 }
            .zip(Zodiac.entries + Zodiac.PISCES) { longitude, zodiac ->
                assertEquals(zodiac, Zodiac.fromTropical(longitude.toDouble()))
            }
    }

    @Test
    fun `Zodiac center of range`() {
        listOf(
            10.88, 42.17, 72.3, 106.46, 127.39, 154.32,
            198.755, 233.37, 256.915, 286.875, 307.105, 330.15
        ).zip(Zodiac.entries) { centerOfZodiac, zodiac ->
            assertThat(zodiac.iauRange.average()).isWithin(1.0e-10).of(centerOfZodiac)
        }
        (0..11).zip(enumValues<Zodiac>()) { it, zodiac ->
            assertThat(zodiac.tropicalRange.average()).isWithin(1.0e-10).of(it * 30.0 + 15)
        }
    }

    @Test
    fun `Season from Persian calendar`() {
        val noLocationSeasons = (1..12).map {
            Season.fromDate(Jdn(PersianDate(1400, it, 28)).toGregorianCalendar().time, null)
        }
        listOf(
            1..3 to Season.SPRING, 4..6 to Season.SUMMER,
            7..9 to Season.AUTUMN, 10..12 to Season.WINTER
        ).forEach { (range, season) ->
            range.forEach { assertThat(noLocationSeasons[it - 1]).isEqualTo(season) }
        }
        val northernHemisphereSeason = (1..12).map {
            val kathmandu = Coordinates(27.7172, 85.324, 1_400.0)
            Season.fromDate(Jdn(PersianDate(1400, it, 28)).toGregorianCalendar().time, kathmandu)
        }
        assertThat(noLocationSeasons).isEqualTo(northernHemisphereSeason)

        val southernHemisphereSeasons = (1..12).map {
            val nirobi = Coordinates(-1.286389, 36.817222, 1_795.0)
            Season.fromDate(Jdn(PersianDate(1400, it, 28)).toGregorianCalendar().time, nirobi)
        }
        listOf(
            1..3 to Season.AUTUMN, 4..6 to Season.WINTER,
            7..9 to Season.SPRING, 10..12 to Season.SUMMER
        ).forEach { (range, season) ->
            range.forEach { assertThat(southernHemisphereSeasons[it - 1]).isEqualTo(season) }
        }
    }

    @Test
    fun `Season equinox`() {
        val seasons = seasons(2020)
        listOf(
            seasons.marchEquinox to 1584676196290, seasons.juneSolstice to 1592689411284,
            seasons.septemberEquinox to 1600781459379, seasons.decemberSolstice to 1608544962334
        ).map { (it, time) -> assertThat(it.toMillisecondsSince1970()).isEqualTo(time) }
    }

    @Test
    fun `Lunar Sunlit Tilt`() {
//        val time = GregorianCalendar(TimeZone.getTimeZone("UTC")).also {
//            it.clear()
//            it.set(2021, Calendar.JANUARY, 10, 4, 0, 0)
//        }
//        assertThat(
//            lunarSunlitTilt(
//                // Equatorial(19.451750, -21.930057, 0.983402),
//                doubleArrayOf(0.338427, -0.847146, -0.367276),
//                Horizontal(187.958846, 40.777947),
//                time,
//                Coordinates(27.0, 85.0, .0)
//            )
//        ).isWithin(1.0e-10).of(-0.9376037778203803)
    }

    @Test
    fun `Iranian animal year name`() {
        assertEquals(
            ChineseZodiac.OX,
            ChineseZodiac.fromPersianCalendar(PersianDate(1400, 1, 1))
        )
        assertEquals(
            ChineseZodiac.TIGER,
            ChineseZodiac.fromPersianCalendar(PersianDate(1401, 1, 1))
        )
        listOf(
            ChineseZodiac.MONKEY, ChineseZodiac.ROOSTER, ChineseZodiac.DOG,
            ChineseZodiac.PIG, ChineseZodiac.RAT, ChineseZodiac.OX,
            ChineseZodiac.TIGER, ChineseZodiac.RABBIT, ChineseZodiac.DRAGON,
            ChineseZodiac.SNAKE, ChineseZodiac.HORSE, ChineseZodiac.GOAT
        ).zip(1395..1406) { expected, year ->
            assertEquals(
                expected, ChineseZodiac.fromPersianCalendar(PersianDate(year, 1, 1))
            )
        }
    }

    @Test
    fun `Chinese animal year name`() {
        // https://en.wikipedia.org/wiki/Chinese_zodiac#Chinese_calendar
        (1..5).flatMap {
            listOf(
                ChineseZodiac.RAT, ChineseZodiac.OX, ChineseZodiac.TIGER,
                ChineseZodiac.RABBIT, ChineseZodiac.DRAGON, ChineseZodiac.SNAKE,
                ChineseZodiac.HORSE, ChineseZodiac.GOAT, ChineseZodiac.MONKEY,
                ChineseZodiac.ROOSTER, ChineseZodiac.DOG, ChineseZodiac.PIG,
            )
        }.zip(1..60) { expected, year ->
            val mock = mock<ChineseCalendar> { on { get(ChineseCalendar.YEAR) } doReturn year }
            assertEquals(expected, ChineseZodiac.fromChineseCalendar(mock))
        }
    }
}
