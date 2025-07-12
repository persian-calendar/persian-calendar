package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.LunarAge
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.byagowi.persiancalendar.ui.astronomy.houses
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
            assertEquals(it, lunarAge.days, 1.0e-5)
            assertEquals((index + 1) % 8, lunarAge.toPhase().ordinal)
        }
        val time = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        time.clear()
        time.set(2022, 1, 13, 0, 0, 0)
//        assertEquals(11.31, $(time, null, 0.0).lunarAge.days, 1.0e-2)
//        assertEquals(12, $(time, null, 0.0).lunarAge.tithi)
//        assertEquals(180.0, $(time, null, 0.0).lunarSunlitTilt, 1.0e0)

//        val kathmandu = Coordinates(27.7172, 85.324, 1_400.0)
//        assertEquals(11.31, $(time, kathmandu, 0.0).lunarAge.days, 1.0e-2)
//        assertEquals(12, $(time, kathmandu, 0.0).lunarAge.tithi)
//        assertEquals(180.0, $(time, kathmandu, 0.0).lunarSunlitTilt, 1.0e0)

//        val nirobi = Coordinates(-1.286389, 36.817222, 1_795.0)
//        assertEquals(.0, $(time, nirobi, 0.0).lunarSunlitTilt, 1.0e0)
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
            assertEquals(centerOfZodiac, zodiac.iauRange.average(), 1.0e-10)
        }
        (0..11).zip(enumValues<Zodiac>()) { it, zodiac ->
            assertEquals(it * 30.0 + 15, zodiac.tropicalRange.average(), 1.0e-10)
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
            range.forEach { assertEquals(season, noLocationSeasons[it - 1]) }
        }
        val northernHemisphereSeason = (1..12).map {
            val kathmandu = Coordinates(27.7172, 85.324, 1_400.0)
            Season.fromDate(Jdn(PersianDate(1400, it, 28)).toGregorianCalendar().time, kathmandu)
        }
        assertEquals(northernHemisphereSeason, noLocationSeasons)

        val southernHemisphereSeasons = (1..12).map {
            val nirobi = Coordinates(-1.286389, 36.817222, 1_795.0)
            Season.fromDate(Jdn(PersianDate(1400, it, 28)).toGregorianCalendar().time, nirobi)
        }
        listOf(
            1..3 to Season.AUTUMN, 4..6 to Season.WINTER,
            7..9 to Season.SPRING, 10..12 to Season.SUMMER
        ).forEach { (range, season) ->
            range.forEach { assertEquals(season, southernHemisphereSeasons[it - 1]) }
        }
    }

    @Test
    fun `Season equinox`() {
        val seasons = seasons(2020)
        listOf(
            seasons.marchEquinox to 1584676196290, seasons.juneSolstice to 1592689411284,
            seasons.septemberEquinox to 1600781459379, seasons.decemberSolstice to 1608544962334
        ).map { (it, time) -> assertEquals(time, it.toMillisecondsSince1970()) }
    }

    @Test
    fun `Lunar Sunlit Tilt`() {
//        val time = GregorianCalendar(TimeZone.getTimeZone("UTC")).also {
//            it.clear()
//            it.set(2021, Calendar.JANUARY, 10, 4, 0, 0)
//        }
//        assertEquals(
//            -0.9376037778203803,
//            lunarSunlitTilt(
//                // Equatorial(19.451750, -21.930057, 0.983402),
//                doubleArrayOf(0.338427, -0.847146, -0.367276),
//                Horizontal(187.958846, 40.777947),
//                time,
//                Coordinates(27.0, 85.0, .0)
//            ),
//            1.0e-10
//        )
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

//    @Test
//    fun `Chinese animal year name`() {
//        // https://en.wikipedia.org/wiki/Chinese_zodiac#Chinese_calendar
//        (1..5).flatMap {
//            listOf(
//                ChineseZodiac.RAT, ChineseZodiac.OX, ChineseZodiac.TIGER,
//                ChineseZodiac.RABBIT, ChineseZodiac.DRAGON, ChineseZodiac.SNAKE,
//                ChineseZodiac.HORSE, ChineseZodiac.GOAT, ChineseZodiac.MONKEY,
//                ChineseZodiac.ROOSTER, ChineseZodiac.DOG, ChineseZodiac.PIG,
//            )
//        }.zip(1..60) { expected, year ->
//            val mock = mock<ChineseCalendar> { on { get(ChineseCalendar.YEAR) } doReturn year }
//            assertEquals(expected, ChineseZodiac.fromChineseCalendar(mock))
//        }
//    }

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
        val ascendants = listOf(
            165.29847801886166, 236.29668499183026, 321.797191638284,
            80.34263360923649, 153.48105769394334, 225.9653124106477,
            304.6980552072615, 64.62545992403068, 143.33024864679658,
            215.11313481654395, 291.38637627325454, 54.76578575422565,
            134.72543967324845, 207.94257567520026, 282.13323220688267,
            40.57586845190173, 128.0385988488901, 199.39109345375834,
            270.9214212751657, 26.357835345588768, 117.51716101735667,
            190.71799947614733, 263.28083807687597, 9.726529738377508,
            110.0952666028478, 180.80568019929513, 250.47258409845392,
            350.25338333258026, 97.29010414477642, 169.61149223122686,
            241.38395110721706, 329.35143674069417, 88.56361718692915,
            162.50276019387098, 233.7188545299278, 322.55668454433277,
            80.71131124720335, 154.8804221204217, 227.34730063093093,
            305.90002157601714, 69.70791148916334
        )
        val midheavens = listOf(
            73.6674692915762, 156.8359554622392, 246.5253026081649,
            329.55822788765704, 60.13096053952552, 143.5468865952335,
            233.8099812381863, 314.14168287940015, 48.0104581652713,
            130.0971717634887, 222.19307876778737, 305.7612554619727,
            37.327133874385936, 121.53875535246021, 213.04094212400594,
            295.12340178053773, 28.787566137967417, 111.64248747789384,
            200.71639167251874, 285.7116593454741, 15.07595653342105,
            101.87160094907495, 191.60194463824791, 275.66013578654133,
            5.373472585234822, 90.88935102525966, 175.36387856929116,
            264.3277360284461, 349.1018998359573, 78.49561787403366,
            163.48287952332885, 251.54926299869965, 338.69238641161337,
            70.51027198602742, 153.4841648259337, 247.0387030539855,
            329.96107258615484, 61.762612284235615, 145.29968044985748,
            234.77117761711645, 318.8433129667867
        )
        assertAll((1380..1420).mapIndexed { i, year ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            val houses = houses(35.68, 51.42, time);
            {
                assertEquals(ascendants[i], houses[0], 1.0e-5, "$year")
                assertEquals(midheavens[i], houses[9], 1.0e-5, "$year")
            }
        })
        // Smoke test
        (1300..1500).forEach { year ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            houses(35.68, 51.42, time)
        }
    }

    @Test
    fun `Test known ascendants`() {
        assertAll(
            listOf(
                1225 to Zodiac.CAPRICORN, // https://w.wiki/EhPH
                1243 to Zodiac.CANCER, // https://w.wiki/EhPK https://w.wiki/EhPN
                1269 to Zodiac.LIBRA, // https://w.wiki/EhPQ

                1272 to Zodiac.CANCER, // https://w.wiki/EhPc
                // 1273 to Zodiac.VIRGO, // doesn't match https://w.wiki/EhPY
                1274 to Zodiac.SAGITTARIUS, // https://w.wiki/EhPX
                1275 to Zodiac.PISCES, // https://w.wiki/EhPV but doesn't match with https://w.wiki/EhPR
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
                val ascendant = houses(35.68, 51.42, time)[0]
                println("$year: ${ascendant % 30}");
                { assertEquals(sign, Zodiac.fromTropical(ascendant), "$year") }
            }
        )
    }

    @Test
    fun `Test against known ascedants`() {
        val time = seasons(CivilDate(PersianDate(1404, 1, 1)).year).marchEquinox
        // These numbers are from swisseph and brought here just for the sake of test
        val expectations = listOf(
            110.08328836357673, 131.1447801879015, 155.4061450733191,
            185.3515114970782, 221.0773712487118, 257.7365979497495,
            290.08328836357674, 311.14478018790146, 335.40614507331907,
            5.351511497078217, 41.0773712487118, 77.73659794974952,
        )
        assertAll(
            expectations.zip(houses(35.68, 51.42, time)) { expected, actual ->
                { assertEquals(expected, actual, 0.025) }
            },
        )
    }

    @Test
    fun `Planetary hours`() {
//        val splits = getDaySplits(1750716418782, Coordinates(35.68, 51.42, 0.0))
//        run {
//            assertEquals(Body.Moon, splits[0].planet.body)
//            assertEquals(4.822251765217631, splits[0].from.value, 1.0e-5)
//            assertEquals(6.036834861898111, splits[0].to.value, 1.0e-5)
//            assertFalse(splits[0].highlighted)
//        }
//        run {
//            val split = splits.first { it.highlighted }
//            assertEquals(Body.Venus, split.planet.body)
//            assertEquals(0.8978681192065139, split.from.value, 1.0e-5)
//            assertEquals(1.6836708611812483, split.to.value, 1.0e-5)
//        }
    }
}
