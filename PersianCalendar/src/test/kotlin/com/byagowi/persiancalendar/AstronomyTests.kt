package com.byagowi.persiancalendar

import android.icu.util.ChineseCalendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.LunarAge
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.byagowi.persiancalendar.ui.astronomy.calculateAscendant
import com.byagowi.persiancalendar.ui.astronomy.calculateMidheaven
import com.google.common.truth.Truth.assertThat
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
            listOf(Zodiac.PISCES) + Zodiac.entries
        ) { longitude, zodiac ->
            assertEquals(
                expected = zodiac,
                actual = Zodiac.fromIau(longitude.toDouble()),
                message = longitude.toString()
            )
        }
        (0..11).map { 20 + it * 30 }
            .zip(Zodiac.entries + Zodiac.PISCES) { longitude, zodiac ->
                assertEquals(zodiac, Zodiac.fromTropical(longitude.toDouble()))
            }
    }

    @Test
    fun `Zodiac center of range`() {
        listOf(
            42.17, 72.3, 106.46, 127.39, 154.32, 198.755,
            233.37, 256.915, 286.875, 307.105, 330.15, 370.88,
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

    @Test
    fun `Southern hemisphere in moon draw`() {
        val southern = Coordinates(-10.0, 10.0, 10.0)
        assertEquals(
            LunarAge.Phase.WAXING_CRESCENT.emoji(null),
            LunarAge.Phase.WANING_CRESCENT.emoji(southern)
        )

        // Just make sure things go well
        (-720..720).map {
            val phase = LunarAge.fromDegrees(it.toDouble()).toPhase()
            "$phase ${phase.emoji(null)} ${phase.emoji(southern)}"
        }
    }

    @Test
    fun `Ascendant correctness`() {
        val expected = listOf(
            165.29847801886166, 236.29668499183026, 321.797191638284,
            80.34263360923649, 153.48105769394334, 225.9653124106477,
            304.6980552072615, 64.62545992403068, 143.33024864679658,
            215.11313481654395, 291.38637627325454, 54.76578575422565,
            134.72543967324845, 207.94257567520026, 282.13323220688267,
            40.57586845190173, 128.0385988488901, 199.39109345375834,
            270.9214212751657, 26.357835345588768, 117.51716101735667,
        )
        assertAll((1380..1400).mapIndexed { i, year ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            val result = calculateAscendant(35.68, 51.42, time);
            { assertEquals(expected[i], result, .1, "$year") }
        })
        // Smoke test
        (1300..1500).forEach { year ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            calculateAscendant(35.68, 51.42, time)
        }
    }

    @Test
    fun `Midheaven correctness`() {
        val expected = listOf(
            72.28739476413239, 158.56694589409585, 244.67001720044186,
            331.6668632833108, 57.955409005311594, 145.87263528159508,
            231.43067114198846, 316.60746336344357, 45.54938891786833,
            132.54248302254933, 219.75197983786268, 308.12962517701135,
            34.97878989562639, 123.77830259687084, 210.82820991496624,
            297.0705530603623, 26.755431559256067, 113.38601405322447,
            199.13703705776845, 287.04520335660277, 13.882593420879289
        )
        assertAll((1380..1400).mapIndexed { i, year ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            val result = calculateMidheaven(51.42, time);
            { assertEquals(expected[i], result, .1, "$year") }
        })
        // Smoke test
        (1300..1500).forEach { year ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            calculateMidheaven(51.42, time)
        }
    }

    @Test
    fun `Test known ascendants`() {
        assertAll(
            listOf(
                1276 to Zodiac.CANCER,
                1277 to Zodiac.VIRGO,
                1278 to Zodiac.SAGITTARIUS,
                1279 to Zodiac.PISCES,
                1280 to Zodiac.GEMINI,
                1281 to Zodiac.VIRGO,
                1282 to Zodiac.SCORPIO,
                1283 to Zodiac.AQUARIUS,
                1284 to Zodiac.GEMINI,
                1285 to Zodiac.VIRGO, // implied
                1286 to Zodiac.SCORPIO,
                1287 to Zodiac.AQUARIUS,
                1288 to Zodiac.GEMINI,
                1289 to Zodiac.LEO,
                1290 to Zodiac.SCORPIO,
                1291 to Zodiac.CAPRICORN,
                1292 to Zodiac.TAURUS,
                1293 to Zodiac.LEO,
                1294 to Zodiac.LIBRA,
                1295 to Zodiac.CAPRICORN,
                1296 to Zodiac.TAURUS,
                1297 to Zodiac.LEO,
                1298 to Zodiac.LIBRA,
                1299 to Zodiac.CAPRICORN,
                1300 to Zodiac.ARIES, // implied
                1301 to Zodiac.CANCER, // implied
                1302 to Zodiac.LIBRA, // implied
                1303 to Zodiac.SAGITTARIUS,
                1304 to Zodiac.ARIES,
                1305 to Zodiac.CANCER,
                1306 to Zodiac.LIBRA, // implied
                1307 to Zodiac.SAGITTARIUS, // implied hardly
                1308 to Zodiac.PISCES, // implied
                1309 to Zodiac.CANCER,
                1310 to Zodiac.VIRGO,
                1311 to Zodiac.SAGITTARIUS,
                1312 to Zodiac.PISCES,
                1313 to Zodiac.GEMINI,
                1314 to Zodiac.VIRGO,
                1315 to Zodiac.SCORPIO,
                1316 to Zodiac.AQUARIUS,
                1317 to Zodiac.GEMINI,
                1318 to Zodiac.VIRGO,
                1319 to Zodiac.SCORPIO,
                1320 to Zodiac.AQUARIUS,
                1321 to Zodiac.GEMINI,
                1322 to Zodiac.LEO,
                1323 to Zodiac.SCORPIO,
                1324 to Zodiac.CAPRICORN,
                // 1325 to Zodiac.TAURUS, doesn't match
                1326 to Zodiac.LEO,
                1327 to Zodiac.LIBRA,
                1328 to Zodiac.CAPRICORN, // implied
                1329 to Zodiac.TAURUS,
                1330 to Zodiac.LEO,

                // 1402 to Zodiac.CANCER, // implied
                // 1403 to Zodiac.LEO, // implied
                1404 to Zodiac.CANCER
            ).map { (year, sign) ->
                val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
                val ascendant = calculateAscendant(35.68, 51.42, time);
                { assertEquals(sign, Zodiac.fromTropical(ascendant), "$year") }
            }
        )
    }
}
