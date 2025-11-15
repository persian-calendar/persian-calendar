package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.astronomy.Zodiac
import com.byagowi.persiancalendar.ui.astronomy.generateMoonInScorpioEntries
import com.byagowi.persiancalendar.utils.MoonInScorpioState
import com.byagowi.persiancalendar.utils.moonInScorpioState
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CalendarLibraryTests {

    @Test
    fun `Islamic calendar calculations correctness`() {
        listOf(
            listOf(2453767, 1427, 1, 1), listOf(2455658, 1432, 5, 2)
//            listOf(2458579, 1440, 7, 29), listOf(2458580, 1440, 8, 1)
        ).forEach {
            val reference = IslamicDate(it[1], it[2], it[3])
            assertEquals(it[0].toLong(), reference.toJdn())
            val converted = IslamicDate(it[0].toLong())

            assertEquals(it[1], converted.year)
            assertEquals(it[2], converted.month)
            assertEquals(it[3], converted.dayOfMonth)

            assertEquals(it[0].toLong(), converted.toJdn())
            assertEquals(reference, IslamicDate(reference.toJdn()))
        }

        listOf(
            listOf(2016, 10, 3, 1438, 1, 1),
            listOf(2016, 11, 1, 1438, 2, 1),
            listOf(2016, 12, 1, 1438, 3, 1),
            listOf(2016, 12, 31, 1438, 4, 1),
            listOf(2016, 10, 3, 1438, 1, 1),
            listOf(2016, 11, 1, 1438, 2, 1),
            listOf(2016, 12, 1, 1438, 3, 1),
            listOf(2016, 12, 31, 1438, 4, 1),
            listOf(2017, 1, 30, 1438, 5, 1),
            listOf(2017, 2, 28, 1438, 6, 1),
            listOf(2017, 3, 30, 1438, 7, 1),
            listOf(2017, 4, 28, 1438, 8, 1),
            listOf(2017, 5, 27, 1438, 9, 1),
            listOf(2017, 6, 26, 1438, 10, 1),
            listOf(2017, 7, 25, 1438, 11, 1),
            listOf(2017, 8, 23, 1438, 12, 1),
            listOf(2017, 9, 22, 1439, 1, 1),
            listOf(2017, 10, 21, 1439, 2, 1),
            listOf(2017, 11, 20, 1439, 3, 1),
            listOf(2017, 12, 20, 1439, 4, 1),
            listOf(2018, 1, 19, 1439, 5, 1),
            listOf(2018, 2, 18, 1439, 6, 1),
            listOf(2018, 3, 19, 1439, 7, 1),
            listOf(2018, 4, 18, 1439, 8, 1),
            listOf(2018, 5, 17, 1439, 9, 1),
            listOf(2018, 6, 15, 1439, 10, 1),
            listOf(2018, 7, 15, 1439, 11, 1),
            listOf(2018, 8, 13, 1439, 12, 1),
            listOf(2018, 9, 11, 1440, 1, 1),
            listOf(2018, 10, 11, 1440, 2, 1),
            listOf(2018, 11, 9, 1440, 3, 1),
            listOf(2018, 12, 9, 1440, 4, 1),
            listOf(2019, 1, 8, 1440, 5, 1),
            listOf(2019, 2, 7, 1440, 6, 1)
//            listOf(2040, 5, 12, 1462, 5, 1),
//            listOf(2040, 6, 11, 1462, 6, 1),
//            listOf(2040, 7, 10, 1462, 7, 1),
//            listOf(2040, 8, 9, 1462, 8, 1),
//            listOf(2040, 9, 7, 1462, 9, 1),
//            listOf(2040, 10, 7, 1462, 10, 1),
//            listOf(2040, 11, 6, 1462, 11, 1),
//            listOf(2040, 12, 5, 1462, 12, 1),
//            listOf(2041, 1, 4, 1463, 1, 1),
//            listOf(2041, 2, 2, 1463, 2, 1),
//            listOf(2041, 3, 4, 1463, 3, 1),
//            listOf(2041, 4, 2, 1463, 4, 1),
//            listOf(2041, 5, 1, 1463, 5, 1),
//            listOf(2041, 5, 31, 1463, 6, 1),
//            listOf(2041, 6, 29, 1463, 7, 1),
//            listOf(2041, 7, 29, 1463, 8, 1),
//            listOf(2041, 8, 28, 1463, 9, 1),
//            listOf(2041, 9, 26, 1463, 10, 1),
//            listOf(2041, 10, 26, 1463, 11, 1),
//            listOf(2041, 11, 25, 1463, 12, 1),
//            listOf(2041, 12, 24, 1464, 1, 1)
        ).forEach {
            val jdn = CivilDate(it[0], it[1], it[2]).toJdn()
            val islamicDate = IslamicDate(it[3], it[4], it[5])

            assertEquals(jdn, islamicDate.toJdn())
            assertEquals(islamicDate, IslamicDate(jdn))
        }

        IslamicDate.useUmmAlQura = true
        listOf(
            listOf(listOf(2015, 3, 14), listOf(1436, 5, 23)),
            listOf(listOf(1999, 4, 1), listOf(1419, 12, 15)),
            listOf(listOf(1989, 2, 25), listOf(1409, 7, 19))
        ).forEach {
            val jdn = CivilDate(it[0][0], it[0][1], it[0][2]).toJdn()
            val islamicDate = IslamicDate(it[1][0], it[1][1], it[1][2])

            assertEquals(jdn, islamicDate.toJdn())
            assertEquals(islamicDate, IslamicDate(jdn))
        }
        IslamicDate.useUmmAlQura = false

        //        int i = -1;
        //        long last = 0;
        //        for (int[][] test : tests2) {
        //            if (i % 12 == 0) {
        //                System.out.print(test[1][0]);
        //                System.out.print(", ");
        //            }
        //            long jdn = DateConverter.toJdn(test[0][0], test[0][1], test[0][2]);
        //            System.out.print(jdn - last);
        //            last = jdn;
        //            System.out.print(", ");
        //            if (i % 12 == 11)
        //                System.out.print("\n");
        //            ++i;
        //        }
    }

    @Test
    fun `Persian calendar converting to and from Jdn practice`() {
        assertEquals(PersianDate(1398, 1, 1).toJdn(), 2458564)
        val startJdn = CivilDate(1750, 1, 1).toJdn()
        val endJdn = CivilDate(2350, 1, 1).toJdn()
        (startJdn..endJdn).forEach { assertEquals(it, PersianDate(it).toJdn()) }
    }

    @Test
    fun `Islamic calendar converting to and from Jdn practice`() {
        val startJdn = CivilDate(1750, 1, 1).toJdn()
        val endJdn = CivilDate(2350, 1, 1).toJdn()
        (startJdn..endJdn).forEach { assertEquals(it, IslamicDate(it).toJdn()) }
    }

    @Test
    fun `UmmAlqara calendar converting to and from Jdn practice`() {
        IslamicDate.useUmmAlQura = true
        val startJdn = CivilDate(1750, 1, 1).toJdn()
        val endJdn = CivilDate(2350, 1, 1).toJdn()
        (startJdn..endJdn).forEach { assertEquals(it, IslamicDate(it).toJdn()) }
        IslamicDate.useUmmAlQura = false
    }

    @Test
    fun `Gregorian calendar converting to and from Jdn practice`() {
        val startJdn = CivilDate(1750, 1, 1).toJdn()
        val endJdn = CivilDate(2350, 1, 1).toJdn()
        (startJdn..endJdn).forEach { assertEquals(it, CivilDate(it).toJdn()) }
    }

    // Combination of:
    // * Borji: https://github.com/user-attachments/assets/6f236e58-a946-4235-8795-886d062868d0
    // * Falaki: https://github.com/user-attachments/assets/c90873ad-6e0b-4a7f-abeb-ec81a3f55f5f
    // Our Falaki ending angle is a bit higher than Iranian one (268 vs 271) that seems to match better
    // with sources elsewhere
    private val moonInScorpioDaysOf1404 = mapOf(
        Jdn(PersianDate(1403, 12, 27)) to MoonInScorpioState.Start(Clock(11.006065277777777)),
        Jdn(PersianDate(1403, 12, 28)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1403, 12, 29)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1403, 12, 30)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1403, 12, 31)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 1, 1)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 1, 2)) to MoonInScorpioState.End(Clock(13.399764166666667)),
        Jdn(PersianDate(1404, 1, 24)) to MoonInScorpioState.Start(Clock(17.399724444444445)),
        Jdn(PersianDate(1404, 1, 25)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 1, 26)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 1, 27)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 1, 28)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 1, 29)) to MoonInScorpioState.End(Clock(20.15809388888889)),
        Jdn(PersianDate(1404, 2, 20)) to MoonInScorpioState.Start(Clock(23.470331666666667)),
        Jdn(PersianDate(1404, 2, 21)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 2, 22)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 2, 23)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 2, 24)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 2, 25)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 2, 26)) to MoonInScorpioState.End(Clock(1.9176055555555556)),
        Jdn(PersianDate(1404, 3, 17)) to MoonInScorpioState.Start(Clock(5.877100555555556)),
        Jdn(PersianDate(1404, 3, 18)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 3, 19)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 3, 20)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 3, 21)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 3, 22)) to MoonInScorpioState.End(Clock(7.848876111111111)),
        Jdn(PersianDate(1404, 4, 13)) to MoonInScorpioState.Start(Clock(13.048908888888889)),
        Jdn(PersianDate(1404, 4, 14)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 4, 15)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 4, 16)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 4, 17)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 4, 18)) to MoonInScorpioState.End(Clock(14.829006388888889)),
        Jdn(PersianDate(1404, 5, 9)) to MoonInScorpioState.Start(Clock(20.91584472222222)),
        Jdn(PersianDate(1404, 5, 10)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 5, 11)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 5, 12)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 5, 13)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 5, 14)) to MoonInScorpioState.End(Clock(22.989675)),
        Jdn(PersianDate(1404, 6, 6)) to MoonInScorpioState.Start(Clock(4.952926111111111)),
        Jdn(PersianDate(1404, 6, 7)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 6, 8)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 6, 9)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 6, 10)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 6, 11)) to MoonInScorpioState.End(Clock(7.693960555555556)),
        Jdn(PersianDate(1404, 7, 2)) to MoonInScorpioState.Start(Clock(12.504638333333334)),
        Jdn(PersianDate(1404, 7, 3)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 7, 4)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 7, 5)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 7, 6)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 7, 7)) to MoonInScorpioState.End(Clock(15.90966)),
        Jdn(PersianDate(1404, 7, 29)) to MoonInScorpioState.Start(Clock(19.202582777777778)),
        Jdn(PersianDate(1404, 7, 30)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 8, 1)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 8, 2)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 8, 3)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 8, 4)) to MoonInScorpioState.End(Clock(22.90885972222222)),
        Jdn(PersianDate(1404, 8, 27)) to MoonInScorpioState.Start(Clock(1.2387016666666666)),
        Jdn(PersianDate(1404, 8, 28)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 8, 29)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 8, 30)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 9, 1)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 9, 2)) to MoonInScorpioState.End(Clock(4.898558055555555)),
        Jdn(PersianDate(1404, 9, 24)) to MoonInScorpioState.Start(Clock(7.352618611111111)),
        Jdn(PersianDate(1404, 9, 25)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 9, 26)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 9, 27)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 9, 28)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 9, 29)) to MoonInScorpioState.End(Clock(10.880463333333333)),
        Jdn(PersianDate(1404, 10, 21)) to MoonInScorpioState.Start(Clock(14.420575833333332)),
        Jdn(PersianDate(1404, 10, 22)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 10, 23)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 10, 24)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 10, 25)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 10, 26)) to MoonInScorpioState.End(Clock(17.780451111111113)),
        Jdn(PersianDate(1404, 11, 18)) to MoonInScorpioState.Start(Clock(22.71645138888889)),
        Jdn(PersianDate(1404, 11, 19)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 11, 20)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 11, 21)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 11, 22)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 11, 23)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 11, 24)) to MoonInScorpioState.End(Clock(1.7442277777777777)),
        Jdn(PersianDate(1404, 12, 16)) to MoonInScorpioState.Start(Clock(7.52313)),
        Jdn(PersianDate(1404, 12, 17)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 12, 18)) to MoonInScorpioState.Borji,
        Jdn(PersianDate(1404, 12, 19)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 12, 20)) to MoonInScorpioState.Falaki,
        Jdn(PersianDate(1404, 12, 21)) to MoonInScorpioState.End(Clock(10.143050555555556)),
    )

    @Test
    fun `moon in scorpio calculation correctness`() {
        run {
            val from = Jdn(PersianDate(1403, 12, 25))
            val to = Jdn(PersianDate(1405, 1, 1))
            (from..to)
        }.forEach { jdn ->
            assertEquals(
                moonInScorpioDaysOf1404[jdn],
                moonInScorpioState(jdn, setIranTime = true),
                "${jdn.toPersianDate()}"
            )
        }
    }

    @Test
    fun `Test generateMoonInScorpioEntries`() {
        // Should more or less match the links
        // https://github.com/user-attachments/assets/6f236e58-a946-4235-8795-886d062868d0
        run {
            val entries = generateMoonInScorpioEntries(1404, Zodiac.SCORPIO.tropicalRange, true)
            assertEquals(12, entries.size)
        }
        // https://github.com/user-attachments/assets/c90873ad-6e0b-4a7f-abeb-ec81a3f55f5f
        run {
            val entries = generateMoonInScorpioEntries(1404, Zodiac.SCORPIO.iauRange, true)
            assertEquals(13, entries.size)
        }
    }

    @Test
    fun `it differentiate date object equal`() {
        assertNotEquals(
            CivilDate(2000, 1, 1) as AbstractDate,
            PersianDate(2000, 1, 1) as AbstractDate
        )
        assertEquals(CivilDate(2000, 1, 1), CivilDate(2000, 1, 1))
        assertNotEquals(CivilDate(2000, 1, 1), CivilDate(2000, 2, 1))
    }

    @Test
    fun `partially conforming with calendariale tests`() {
        val J0000 = 1721425L // Ours is different apparently
        listOf(
//        listOf(-214193, -1208, 5, 1),
//        listOf(-61387, -790, 9, 14),
//        listOf(25469, -552, 7, 2),
//        listOf(49217, -487, 7, 9),
//        listOf(171307, -153, 10, 18),
//        listOf(210155, -46, 2, 30),
//            listOf(253427, 73, 8, 19),
            listOf(369740, 392, 2, 5),
//            listOf(400085, 475, 3, 3),
            listOf(434355, 569, 1, 3),
//            listOf(452605, 618, 12, 20),
            listOf(470160, 667, 1, 14),
            listOf(473837, 677, 2, 8),
            listOf(507850, 770, 3, 22),
//            listOf(524156, 814, 11, 13),
            listOf(544676, 871, 1, 21),
//            listOf(567118, 932, 6, 28),
//            listOf(569477, 938, 12, 14),
            listOf(601716, 1027, 3, 21),
            listOf(613424, 1059, 4, 10),
            listOf(626596, 1095, 5, 2),
            listOf(645554, 1147, 3, 30),
            listOf(664224, 1198, 5, 10),
            listOf(671401, 1218, 1, 7),
            listOf(694799, 1282, 1, 29),
            listOf(704424, 1308, 6, 3),
            listOf(708842, 1320, 7, 7),
            listOf(709409, 1322, 1, 29),
            listOf(709580, 1322, 7, 14),
            listOf(727274, 1370, 12, 27),
            listOf(728714, 1374, 12, 6),
            listOf(744313, 1417, 8, 19),
            listOf(764652, 1473, 4, 28)
        ).forEach {
            assertEquals(it[0] + J0000, PersianDate(it[1], it[2], it[3]).toJdn(), it.toString())
            val from = PersianDate(it[0] + J0000)
            assertEquals(from.year, it[1])
            assertEquals(from.month, it[2])
            assertEquals(from.dayOfMonth, it[3])
        }

        listOf(
//        listOf(1507231, -586, 7, 24),
//        listOf(1660037, -168, 12, 5),
//        listOf(1746893, 70, 9, 24),
//        listOf(1770641, 135, 10, 2),
//        listOf(1892731, 470, 1, 8),
//        listOf(1931579, 576, 5, 20),
//        listOf(1974851, 694, 11, 10),
//        listOf(2091164, 1013, 4, 25),
//        listOf(2121509, 1096, 5, 24),
//        listOf(2155779, 1190, 3, 23),
//        listOf(2174029, 1240, 3, 10),
//        listOf(2191584, 1288, 4, 2),
//        listOf(2195261, 1298, 4, 27),
//        listOf(2229274, 1391, 6, 12),
//        listOf(2245580, 1436, 2, 3),
//        listOf(2266100, 1492, 4, 9),
//        listOf(2288542, 1553, 9, 19),
//        listOf(2290901, 1560, 3, 5),
//        listOf(2323140, 1648, 6, 10),
            listOf(2334848, 1680, 6, 30),
            listOf(2348020, 1716, 7, 24),
            listOf(2366978, 1768, 6, 19),
            listOf(2385648, 1819, 8, 2),
            listOf(2392825, 1839, 3, 27),
            listOf(2416223, 1903, 4, 19),
            listOf(2425848, 1929, 8, 25),
            listOf(2430266, 1941, 9, 29),
            listOf(2430833, 1943, 4, 19),
            listOf(2431004, 1943, 10, 7),
            listOf(2448698, 1992, 3, 17),
            listOf(2450138, 1996, 2, 25),
            listOf(2465737, 2038, 11, 10),
            listOf(2486076, 2094, 7, 18)
        ).forEach {
            assertEquals(it[0] + 1L, CivilDate(it[1], it[2], it[3]).toJdn())
            val from = CivilDate(it[0] + 1L)
            assertEquals(from.year, it[1])
            assertEquals(from.month, it[2])
            assertEquals(from.dayOfMonth, it[3])
        }

        listOf(
//        listOf(-214193, -1245, 12, 11),
//        listOf(-61387, -813, 2, 25),
//        listOf(25469, -568, 4, 2),
//        listOf(49217, -501, 4, 7),
//        listOf(171307, -157, 10, 18),
//        listOf(210155, -47, 6, 3),
//        listOf(253427, 75, 7, 13),
//        listOf(369740, 403, 10, 5),
//        listOf(400085, 489, 5, 22),
//        listOf(434355, 586, 2, 7),
            listOf(452605, 637, 8, 7),
//        listOf(470160, 687, 2, 21),
//        listOf(473837, 697, 7, 7),
//        listOf(507850, 793, 6, 30),
//        listOf(524156, 839, 7, 6),
//        listOf(544676, 897, 6, 2),
//        listOf(567118, 960, 9, 30),
//        listOf(569477, 967, 5, 27),
//        listOf(601716, 1058, 5, 18),
//        listOf(613424, 1091, 6, 3),
//        listOf(626596, 1128, 8, 4),
            listOf(645554, 1182, 2, 4),
//        listOf(664224, 1234, 10, 10),
//        listOf(671401, 1255, 1, 11),
//        listOf(694799, 1321, 1, 20),
            listOf(704424, 1348, 3, 19),
//        listOf(708842, 1360, 9, 7),
//        listOf(709409, 1362, 4, 14),
//        listOf(709580, 1362, 10, 7),
//        listOf(727274, 1412, 9, 12),
//        listOf(728714, 1416, 10, 5),
//        listOf(744313, 1460, 10, 12),
            listOf(764652, 1518, 3, 5)
        ).forEach {
            assertEquals(it[0] + J0000, IslamicDate(it[1], it[2], it[3]).toJdn(), "${it[0]}")
            val from = IslamicDate(it[0] + J0000)
            assertEquals(from.year, it[1])
            assertEquals(from.month, it[2])
            assertEquals(from.dayOfMonth, it[3])
        }
    }

    @Test
    fun `TimeSlots works`() {
        ((24 * -10)..(24 * 10)).map {
            val clock = Clock(it / 10.0)
            "${clock.toHoursAndMinutesPair()}: ${clock.timeSlot}"
        }
        (0..(24 * 2)).forEach {
            val clock = Clock(it / 2.0)
            "${clock.toHoursAndMinutesPair()}: ${clock.timeSlot}"
        }
        listOf(
            2.0 to Clock.TimeSlot.Dawn,
            4.0 to Clock.TimeSlot.Dawn,
            7.0 to Clock.TimeSlot.Morning,
            10.0 to Clock.TimeSlot.Midday,
            15.0 to Clock.TimeSlot.Sunset,
            19.0 to Clock.TimeSlot.Evening,
            22.0 to Clock.TimeSlot.Dusk,
            1.0 to Clock.TimeSlot.Dusk,
            1.5 to Clock.TimeSlot.Dusk,
        ).map { (hour, slot) ->
            { assertEquals(slot, Clock(hour).timeSlot, "$hour") }
        }.let(::assertAll)
    }
}
