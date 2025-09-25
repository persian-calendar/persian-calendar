package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.Compatibility.BEST
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.Compatibility.BETTER
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.Compatibility.NEUTRAL
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.Compatibility.WORSE
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.Compatibility.WORST
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.DOG
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.DRAGON
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.EARTH
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.FIRE
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.METAL
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.WATER
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.FixedElement.WOOD
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.GOAT
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.HORSE
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.MONKEY
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.OX
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.PIG
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.RABBIT
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.RAT
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.ROOSTER
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.SNAKE
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac.TIGER
import com.byagowi.persiancalendar.ui.astronomy.LunarAge
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.byagowi.persiancalendar.ui.astronomy.houses
import com.byagowi.persiancalendar.ui.astronomy.meanApogee
import com.byagowi.persiancalendar.ui.astronomy.toAbjad
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.test.assertContains
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
        assertEquals(LunarAge.Phase.NEW_MOON, LunarAge.fromDegrees(340.0).toPhase())
        assertEquals(LunarAge.Phase.NEW_MOON, LunarAge.fromDegrees(0.0).toPhase())
        assertEquals(LunarAge.Phase.FIRST_QUARTER, LunarAge.fromDegrees(90.0).toPhase())
        assertEquals(LunarAge.Phase.FULL_MOON, LunarAge.fromDegrees(180.0).toPhase())
        assertEquals(LunarAge.Phase.THIRD_QUARTER, LunarAge.fromDegrees(270.0).toPhase())
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
            OX,
            ChineseZodiac.fromPersianCalendar(PersianDate(1400, 1, 1))
        )
        assertEquals(
            TIGER,
            ChineseZodiac.fromPersianCalendar(PersianDate(1401, 1, 1))
        )
        listOf(
            MONKEY, ROOSTER, DOG,
            PIG, RAT, OX,
            TIGER, RABBIT, DRAGON,
            SNAKE, HORSE, GOAT
        ).zip(1395..1406) { expected, year ->
            assertEquals(
                expected, ChineseZodiac.fromPersianCalendar(PersianDate(year, 1, 1))
            )
        }
        // Just doesn't crash in negative years
        (-20..20).forEach { year ->
            ChineseZodiac.fromPersianCalendar(PersianDate(year, 1, 1))
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
        (1380..1420).mapIndexed { i, year ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            val houses = houses(35.68, 51.42, time);
            {
                assertEquals(ascendants[i], houses[0], 1.0e-5, "$year")
                assertEquals(midheavens[i], houses[9], 1.0e-5, "$year")
            }
        }.run(::assertAll)
        // Smoke test
        (1300..1500).forEach { year ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            houses(35.68, 51.42, time)
        }
    }

    fun Zodiac.with(degrees: Int, minutes: Int): Double = ordinal * 30 + degrees + minutes / 60.0

    @Test
    fun `Check black moon values`() {
        listOf(
            1398 to Zodiac.AQUARIUS.with(25, 7),
            1400 to Zodiac.TAURUS.with(16, 42),
            1401 to Zodiac.GEMINI.with(27, 11),
            1402 to Zodiac.LEO.with(7, 59),
            1403 to Zodiac.VIRGO.with(18, 46),
        ).map { (year, expected) ->
            val time = seasons(CivilDate(PersianDate(year, 1, 1)).year).marchEquinox
            { assertEquals(expected, meanApogee(time), .15, "$year") }
        }.run(::assertAll)
    }

    @Test
    fun `Test known ascendants`() {
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
        }.run(::assertAll)
    }

    @Test
    fun `Test against known ascendants`() {
        val time = seasons(CivilDate(PersianDate(1404, 1, 1)).year).marchEquinox
        // These numbers are from swisseph and brought here just for the sake of test
        val expectations = listOf(
            110.08328836357673, 131.1447801879015, 155.4061450733191,
            185.3515114970782, 221.0773712487118, 257.7365979497495,
            290.08328836357674, 311.14478018790146, 335.40614507331907,
            5.351511497078217, 41.0773712487118, 77.73659794974952,
        )
        expectations.zip(houses(35.68, 51.42, time)) { expected, actual ->
            { assertEquals(expected, actual, 0.025) }
        }.run(::assertAll)
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

    /*
     * https://en.wikipedia.org/wiki/Chinese_zodiac#Compatibility
     *
     * |   Sign  |      Best Match      |                    Average Match                   | Super Bad | Harmful |
     * |:-------:|:--------------------:|:--------------------------------------------------:|:---------:|---------|
     * | Rat     | Dragon, Monkey, Ox   | Pig, Tiger, Dog, Snake, Rabbit, Rooster, Rat       | Horse     | Goat    |
     * | Ox      | Rooster, Snake, Rat  | Monkey, Dog, Rabbit, Tiger, Dragon, Pig, Ox        | Goat      | Horse   |
     * | Tiger   | Horse, Dog, Pig      | Rabbit, Dragon, Rooster, Rat, Goat, Ox, Tiger      | Monkey    | Snake   |
     * | Rabbit  | Pig, Goat, Dog       | Tiger, Monkey, Rabbit, Ox, Horse, Rat, Snake       | Rooster   | Dragon  |
     * | Dragon  | Rat, Monkey, Rooster | Tiger, Snake, Horse, Goat, Pig, Ox, Dragon         | Dog       | Rabbit  |
     * | Snake   | Ox, Rooster, Monkey  | Horse, Dragon, Goat, Dog, Rabbit, Rat, Snake       | Pig       | Tiger   |
     * | Horse   | Dog, Tiger, Goat     | Snake, Rabbit, Dragon, Rooster, Pig, Monkey, Horse | Rat       | Ox      |
     * | Goat    | Rabbit, Pig, Horse   | Snake, Goat, Dragon, Monkey, Rooster, Dog, Tiger   | Ox        | Rat     |
     * | Monkey  | Dragon, Rat, Snake   | Monkey, Dog, Ox, Goat, Rabbit, Rooster, Horse      | Tiger     | Pig     |
     * | Rooster | Snake, Ox, Dragon    | Horse, Rooster, Goat, Pig, Tiger, Monkey, Rat      | Rabbit    | Dog     |
     * | Dog     | Tiger, Horse, Rabbit | Monkey, Pig, Rat, Ox, Snake, Goat, Dog             | Dragon    | Rooster |
     * | Pig     | Rabbit, Goat, Tiger  | Rat, Rooster, Dog, Dragon, Horse, Ox, Pig          | Snake     | Monkey  |
     */
    @Test
    fun `Matches with compatibility table`() {
        listOf(
            setOf(DRAGON, MONKEY, OX),
            setOf(ROOSTER, SNAKE, RAT),
            setOf(HORSE, DOG, PIG),
            setOf(PIG, GOAT, DOG),
            setOf(RAT, MONKEY, ROOSTER),
            setOf(OX, ROOSTER, MONKEY),
            setOf(DOG, TIGER, GOAT),
            setOf(RABBIT, PIG, HORSE),
            setOf(DRAGON, RAT, SNAKE),
            setOf(SNAKE, OX, DRAGON),
            setOf(TIGER, HORSE, RABBIT),
            setOf(RABBIT, GOAT, TIGER),
        ).zip(ChineseZodiac.entries) { bestMatch, yearZodiac ->
            bestMatch.map {
                { assertContains(listOf(BEST, BETTER), yearZodiac compatibilityWith it) }
            }
        }.flatten().let(::assertAll)

        listOf(
            setOf(PIG, TIGER, DOG, SNAKE, RABBIT, ROOSTER, RAT),
            setOf(MONKEY, DOG, RABBIT, TIGER, DRAGON, PIG, OX),
            setOf(RABBIT, DRAGON, ROOSTER, RAT, GOAT, OX, TIGER),
            setOf(TIGER, MONKEY, RABBIT, OX, HORSE, RAT, SNAKE),
            setOf(TIGER, SNAKE, HORSE, GOAT, PIG, OX, DRAGON),
            setOf(HORSE, DRAGON, GOAT, DOG, RABBIT, RAT, SNAKE),
            setOf(SNAKE, RABBIT, DRAGON, ROOSTER, PIG, MONKEY, HORSE),
            setOf(SNAKE, GOAT, DRAGON, MONKEY, ROOSTER, DOG, TIGER),
            setOf(MONKEY, DOG, OX, GOAT, RABBIT, ROOSTER, HORSE),
            setOf(HORSE, ROOSTER, GOAT, PIG, TIGER, MONKEY, RAT),
            setOf(MONKEY, PIG, RAT, OX, SNAKE, GOAT, DOG),
            setOf(RAT, ROOSTER, DOG, DRAGON, HORSE, OX, PIG),
        ).zip(ChineseZodiac.entries) { neutralMatches, yearZodiac ->
            neutralMatches.map { { assertEquals(NEUTRAL, yearZodiac compatibilityWith it) } }
        }.flatten().let(::assertAll)

        listOf(HORSE, GOAT, MONKEY, ROOSTER, DOG, PIG, RAT, OX, TIGER, RABBIT, DRAGON, SNAKE).zip(
            ChineseZodiac.entries
        ) { worseMatch, yearZodiac ->
            { assertEquals(WORSE, yearZodiac compatibilityWith worseMatch) }
        }.let(::assertAll)

        listOf(GOAT, HORSE, SNAKE, DRAGON, RABBIT, TIGER, OX, RAT, PIG, DOG, ROOSTER, MONKEY).zip(
            ChineseZodiac.entries
        ) { worstMatch, yearZodiac ->
            { assertEquals(WORST, yearZodiac compatibilityWith worstMatch) }
        }.let(::assertAll)
    }

    /**
     * https://en.wikipedia.org/wiki/Chinese_zodiac#Signs
     *
     * | Number |  Animal | Yin/Yang | Trine | Fixed Element |
     * |:------:|:-------:|:--------:|:-----:|:-------------:|
     * | 1      | Rat     | Yang     | 1st   | Water         |
     * | 2      | Ox      | Yin      | 2nd   | Earth         |
     * | 3      | Tiger   | Yang     | 3rd   | Wood          |
     * | 4      | Rabbit  | Yin      | 4th   | Wood          |
     * | 5      | Dragon  | Yang     | 1st   | Earth         |
     * | 6      | Snake   | Yin      | 2nd   | Fire          |
     * | 7      | Horse   | Yang     | 3rd   | Fire          |
     * | 8      | Goat    | Yin      | 4th   | Earth         |
     * | 9      | Monkey  | Yang     | 1st   | Metal         |
     * | 10     | Rooster | Yin      | 2nd   | Metal         |
     * | 11     | Dog     | Yang     | 3rd   | Earth         |
     * | 12     | Pig     | Yin      | 4th   | Water         |
     */
    @Test
    fun `Matches with misc Chinese Zodiac details`() {
        assertEquals(RAT.yinYang, ChineseZodiac.YinYang.YANG)
        assertEquals(GOAT.yinYang, ChineseZodiac.YinYang.YIN)
        assertEquals(DRAGON.trin, 1)

        listOf(
            WATER, EARTH, WOOD, WOOD, EARTH, FIRE, FIRE, EARTH, METAL, METAL, EARTH, WATER
        ).forEachIndexed { i, element ->
            assertEquals(element, ChineseZodiac.entries[i].fixedElement)
        }
    }

    @Test
    fun `Check Abjad conversion`() {
        listOf(
            1 to "ا",
            2 to "ب",
            3 to "ج",
            4 to "د",
            5 to "ه",
            6 to "و",
            7 to "ز",
            8 to "ح",
            9 to "ط",
            10 to "ی",
            11 to "یا",
            12 to "یب",
            13 to "یج",
            14 to "ید",
            15 to "یه",
            16 to "یو",
            17 to "یز",
            18 to "یح",
            19 to "یط",
            20 to "ک",
            21 to "کا",
            22 to "کب",
            23 to "کج",
            24 to "کد",
            25 to "که",
            26 to "کو",
            27 to "کز",
            28 to "کح",
            29 to "کط",
            30 to "ل",
            31 to "لا",
            //360 to "سش"
        ).map { (input, expected) ->
            { assertEquals(expected, toAbjad(input), "$input") }
        }.let(::assertAll)
    }
}
