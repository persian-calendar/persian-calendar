package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.utils.isMoonInScorpio
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
            assertTrue(reference == IslamicDate(reference.toJdn()))
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
            assertTrue(islamicDate == IslamicDate(jdn))
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

    @Test
    fun `moon in scorpio calculation correctness`() {
        val positiveJdn = listOf(
            listOf(1397, 1, 14), listOf(1397, 1, 15), listOf(1397, 2, 10),
            listOf(1397, 2, 11), listOf(1397, 2, 12), listOf(1397, 3, 6),
            listOf(1397, 3, 7), listOf(1397, 3, 8), listOf(1397, 4, 2),
            listOf(1397, 4, 3), listOf(1397, 4, 30), listOf(1397, 4, 31),
            listOf(1397, 5, 26), listOf(1397, 5, 27), listOf(1397, 6, 22),
            listOf(1397, 6, 23), listOf(1397, 7, 18), listOf(1397, 7, 19),
            listOf(1397, 7, 20), listOf(1397, 8, 16), listOf(1397, 8, 17),
            listOf(1397, 9, 12), listOf(1397, 9, 13), listOf(1397, 9, 14),
            listOf(1397, 10, 10), listOf(1397, 10, 11), listOf(1397, 11, 8),
            listOf(1397, 11, 9), listOf(1397, 12, 6), listOf(1397, 12, 7)
        ).map { PersianDate(it[0], it[1], it[2]).toJdn() }

        val startOfYear = PersianDate(1397, 1, 1).toJdn()
        (0..365).forEach {
            val jdn = startOfYear + it
            val persian = PersianDate(jdn)
            val year = persian.year
            val month = persian.month
            val day = persian.dayOfMonth

            assertEquals(
                jdn in positiveJdn,
                isMoonInScorpio(persian, IslamicDate(jdn)),
                "%d %d %d".format(year, month, day)
            )
        }
    }

    @Test
    fun test_it_different_date_object_equal() {
        assertFalse(CivilDate(2000, 1, 1) == PersianDate(2000, 1, 1))
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
            listOf(253427, 73, 8, 19),
            listOf(369740, 392, 2, 5),
//            listOf(400085, 475, 3, 3),
            listOf(434355, 569, 1, 3),
            listOf(452605, 618, 12, 20),
            listOf(470160, 667, 1, 14),
            listOf(473837, 677, 2, 8),
            listOf(507850, 770, 3, 22),
            listOf(524156, 814, 11, 13),
            listOf(544676, 871, 1, 21),
            listOf(567118, 932, 6, 28),
            listOf(569477, 938, 12, 14),
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
            assertEquals(it[0] + J0000, PersianDate(it[1], it[2], it[3]).toJdn())
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
}
