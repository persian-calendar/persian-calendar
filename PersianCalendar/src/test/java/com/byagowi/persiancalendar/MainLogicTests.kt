package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.calendar.CivilDate
import com.byagowi.persiancalendar.calendar.IslamicDate
import com.byagowi.persiancalendar.calendar.PersianDate
import com.byagowi.persiancalendar.equinox.Equinox
import com.byagowi.persiancalendar.praytimes.CalculationMethod
import com.byagowi.persiancalendar.praytimes.Clock
import com.byagowi.persiancalendar.praytimes.Coordinate
import com.byagowi.persiancalendar.praytimes.PrayTimesCalculator
import com.byagowi.persiancalendar.utils.AstronomicalUtils
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.Utils
import com.cepmuvakkit.times.view.QiblaCompassView
import org.junit.Assert.*
import org.junit.Test
//import us.fatehi.calculation.Equinox
import java.util.*

class MainLogicTests {
    @Test
    fun islamic_converter_test() {
        arrayOf(
                arrayOf(2453767, 1427, 1, 1), arrayOf(2455658, 1432, 5, 2)
//            arrayOf(2458579, 1440, 7, 29), arrayOf(2458580, 1440, 8, 1)
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

        arrayOf(
                arrayOf(2016, 10, 3, 1438, 1, 1),
                arrayOf(2016, 11, 1, 1438, 2, 1),
                arrayOf(2016, 12, 1, 1438, 3, 1),
                arrayOf(2016, 12, 31, 1438, 4, 1),
                arrayOf(2016, 10, 3, 1438, 1, 1),
                arrayOf(2016, 11, 1, 1438, 2, 1),
                arrayOf(2016, 12, 1, 1438, 3, 1),
                arrayOf(2016, 12, 31, 1438, 4, 1),
                arrayOf(2017, 1, 30, 1438, 5, 1),
                arrayOf(2017, 2, 28, 1438, 6, 1),
                arrayOf(2017, 3, 30, 1438, 7, 1),
                arrayOf(2017, 4, 28, 1438, 8, 1),
                arrayOf(2017, 5, 27, 1438, 9, 1),
                arrayOf(2017, 6, 26, 1438, 10, 1),
                arrayOf(2017, 7, 25, 1438, 11, 1),
                arrayOf(2017, 8, 23, 1438, 12, 1),
                arrayOf(2017, 9, 22, 1439, 1, 1),
                arrayOf(2017, 10, 21, 1439, 2, 1),
                arrayOf(2017, 11, 20, 1439, 3, 1),
                arrayOf(2017, 12, 20, 1439, 4, 1),
                arrayOf(2018, 1, 19, 1439, 5, 1),
                arrayOf(2018, 2, 18, 1439, 6, 1),
                arrayOf(2018, 3, 19, 1439, 7, 1),
                arrayOf(2018, 4, 18, 1439, 8, 1),
                arrayOf(2018, 5, 17, 1439, 9, 1),
                arrayOf(2018, 6, 15, 1439, 10, 1),
                arrayOf(2018, 7, 15, 1439, 11, 1),
                arrayOf(2018, 8, 13, 1439, 12, 1),
                arrayOf(2018, 9, 11, 1440, 1, 1),
                arrayOf(2018, 10, 11, 1440, 2, 1),
                arrayOf(2018, 11, 9, 1440, 3, 1),
                arrayOf(2018, 12, 9, 1440, 4, 1),
                arrayOf(2019, 1, 8, 1440, 5, 1),
                arrayOf(2019, 2, 7, 1440, 6, 1)
//            arrayOf(2040, 5, 12, 1462, 5, 1),
//            arrayOf(2040, 6, 11, 1462, 6, 1),
//            arrayOf(2040, 7, 10, 1462, 7, 1),
//            arrayOf(2040, 8, 9, 1462, 8, 1),
//            arrayOf(2040, 9, 7, 1462, 9, 1),
//            arrayOf(2040, 10, 7, 1462, 10, 1),
//            arrayOf(2040, 11, 6, 1462, 11, 1),
//            arrayOf(2040, 12, 5, 1462, 12, 1),
//            arrayOf(2041, 1, 4, 1463, 1, 1),
//            arrayOf(2041, 2, 2, 1463, 2, 1),
//            arrayOf(2041, 3, 4, 1463, 3, 1),
//            arrayOf(2041, 4, 2, 1463, 4, 1),
//            arrayOf(2041, 5, 1, 1463, 5, 1),
//            arrayOf(2041, 5, 31, 1463, 6, 1),
//            arrayOf(2041, 6, 29, 1463, 7, 1),
//            arrayOf(2041, 7, 29, 1463, 8, 1),
//            arrayOf(2041, 8, 28, 1463, 9, 1),
//            arrayOf(2041, 9, 26, 1463, 10, 1),
//            arrayOf(2041, 10, 26, 1463, 11, 1),
//            arrayOf(2041, 11, 25, 1463, 12, 1),
//            arrayOf(2041, 12, 24, 1464, 1, 1)
        ).forEach {
            val jdn = CivilDate(it[0], it[1], it[2]).toJdn()
            val islamicDate = IslamicDate(it[3], it[4], it[5])

            assertEquals(jdn, islamicDate.toJdn())
            assertTrue(islamicDate == IslamicDate(jdn))
        }

        IslamicDate.useUmmAlQura = true
        arrayOf(
                arrayOf(arrayOf(2015, 3, 14), arrayOf(1436, 5, 23)),
                arrayOf(arrayOf(1999, 4, 1), arrayOf(1419, 12, 15)),
                arrayOf(arrayOf(1989, 2, 25), arrayOf(1409, 7, 19))
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
    fun practice_persian_converting_back_and_forth() {
        assertEquals(PersianDate(1398, 1, 1).toJdn(), 2458564)
        val startJdn = CivilDate(1750, 1, 1).toJdn()
        val endJdn = CivilDate(2350, 1, 1).toJdn()
        (startJdn..endJdn).forEach { assertEquals(it, PersianDate(it).toJdn()) }
    }

    @Test
    fun practice_islamic_converting_back_and_forth() {
        val startJdn = CivilDate(1750, 1, 1).toJdn()
        val endJdn = CivilDate(2350, 1, 1).toJdn()
        (startJdn..endJdn).forEach { assertEquals(it, IslamicDate(it).toJdn()) }
    }

    @Test
    fun practice_ummalqara_converting_back_and_forth() {
        IslamicDate.useUmmAlQura = true
        val startJdn = CivilDate(1750, 1, 1).toJdn()
        val endJdn = CivilDate(2350, 1, 1).toJdn()
        (startJdn..endJdn).forEach { assertEquals(it, IslamicDate(it).toJdn()) }
        IslamicDate.useUmmAlQura = false
    }

    @Test
    fun practice_civil_converting_back_and_forth() {
        val startJdn = CivilDate(1750, 1, 1).toJdn()
        val endJdn = CivilDate(2350, 1, 1).toJdn()
        (startJdn..endJdn).forEach { assertEquals(it, CivilDate(it).toJdn()) }
    }

    @Test
    fun practice_moon_in_scorpio() {
        val positiveJdn = arrayOf(
                arrayOf(1397, 1, 14), arrayOf(1397, 1, 15), arrayOf(1397, 2, 10),
                arrayOf(1397, 2, 11), arrayOf(1397, 2, 12), arrayOf(1397, 3, 6),
                arrayOf(1397, 3, 7), arrayOf(1397, 3, 8), arrayOf(1397, 4, 2),
                arrayOf(1397, 4, 3), arrayOf(1397, 4, 30), arrayOf(1397, 4, 31),
                arrayOf(1397, 5, 26), arrayOf(1397, 5, 27), arrayOf(1397, 6, 22),
                arrayOf(1397, 6, 23), arrayOf(1397, 7, 18), arrayOf(1397, 7, 19),
                arrayOf(1397, 7, 20), arrayOf(1397, 8, 16), arrayOf(1397, 8, 17),
                arrayOf(1397, 9, 12), arrayOf(1397, 9, 13), arrayOf(1397, 9, 14),
                arrayOf(1397, 10, 10), arrayOf(1397, 10, 11), arrayOf(1397, 11, 8),
                arrayOf(1397, 11, 9), arrayOf(1397, 12, 6), arrayOf(1397, 12, 7)
        ).map { PersianDate(it[0], it[1], it[2]).toJdn() }

        val startOfYear = PersianDate(1397, 1, 1).toJdn()
        (0..365).forEach {
            val jdn = startOfYear + it
            val persian = PersianDate(jdn)
            val year = persian.year
            val month = persian.month
            val day = persian.dayOfMonth

            assertEquals(String.format("%d %d %d", year, month, day),
                    positiveJdn.contains(jdn),
                    AstronomicalUtils.isMoonInScorpio(persian, IslamicDate(jdn)))
        }
    }

    @Test
    fun test_getMonthLength() {
        assertEquals(31, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 1))
        assertEquals(31, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 2))
        assertEquals(31, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 3))
        assertEquals(31, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 4))
        assertEquals(31, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 5))
        assertEquals(31, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 6))
        assertEquals(30, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 7))
        assertEquals(30, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 8))
        assertEquals(30, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 9))
        assertEquals(30, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 10))
        assertEquals(30, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 11))
        assertEquals(29, Utils.getMonthLength(CalendarType.SHAMSI, 1397, 12))
    }

    @Test
    fun test_praytimes() {
        // http://praytimes.org/code/v2/js/examples/monthly.htm
        var prayTimes = PrayTimesCalculator.calculate(
                CalculationMethod.MWL,
                Utils.civilDateToCalendar(CivilDate(2018, 9, 5)).time,
                Coordinate(43.0, -80.0, 0.0),
                -5.0, true)

        assertEquals(Clock(5, 9).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 21).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(CalculationMethod.ISNA,
                Utils.civilDateToCalendar(CivilDate(2018, 9, 5)).time,
                Coordinate(43.0, -80.0, 0.0),
                -5.0, true)
        assertEquals(Clock(5, 27).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 9).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(CalculationMethod.Egypt,
                Utils.civilDateToCalendar(CivilDate(2018, 9, 5)).time,
                Coordinate(43.0, -80.0, 0.0),
                -5.0, true)
        assertEquals(Clock(5, 0).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 24).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(CalculationMethod.Makkah,
                Utils.civilDateToCalendar(CivilDate(2018, 9, 5)).time,
                Coordinate(43.0, -80.0, 0.0),
                -5.0, true)
        assertEquals(Clock(5, 6).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 18).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(CalculationMethod.Karachi,
                Utils.civilDateToCalendar(CivilDate(2018, 9, 5)).time,
                Coordinate(43.0, -80.0, 0.0),
                -5.0, true)
        assertEquals(Clock(5, 9).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(19, 48).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 27).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(CalculationMethod.Jafari,
                Utils.civilDateToCalendar(CivilDate(2018, 9, 5)).time,
                Coordinate(43.0, -80.0, 0.0),
                -5.0, true)
        assertEquals(Clock(5, 21).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(20, 5).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 3).toInt(), prayTimes.ishaClock.toInt())

        prayTimes = PrayTimesCalculator.calculate(CalculationMethod.Tehran,
                Utils.civilDateToCalendar(CivilDate(2018, 9, 5)).time,
                Coordinate(43.0, -80.0, 0.0),
                -5.0, true)
        assertEquals(Clock(5, 11).toInt(), prayTimes.fajrClock.toInt())
        assertEquals(Clock(6, 49).toInt(), prayTimes.sunriseClock.toInt())
        assertEquals(Clock(13, 19).toInt(), prayTimes.dhuhrClock.toInt())
        assertEquals(Clock(16, 57).toInt(), prayTimes.asrClock.toInt())
        assertEquals(Clock(20, 8).toInt(), prayTimes.maghribClock.toInt())
        assertEquals(Clock(21, 3).toInt(), prayTimes.ishaClock.toInt())
    }

    @Test
    fun test_isNearToDegree() {
        assertTrue(QiblaCompassView.isNearToDegree(360f, 1f))
        assertTrue(QiblaCompassView.isNearToDegree(1f, 360f))

        assertTrue(QiblaCompassView.isNearToDegree(2f, 360f))
        assertFalse(QiblaCompassView.isNearToDegree(3f, 360f))

        assertTrue(QiblaCompassView.isNearToDegree(360f, 2f))
        assertFalse(QiblaCompassView.isNearToDegree(360f, 3f))

        assertTrue(QiblaCompassView.isNearToDegree(180f, 181f))
        assertTrue(QiblaCompassView.isNearToDegree(180f, 182f))
        assertFalse(QiblaCompassView.isNearToDegree(180f, 183f))
        assertFalse(QiblaCompassView.isNearToDegree(180f, 184f))

        assertTrue(QiblaCompassView.isNearToDegree(181f, 180f))
        assertTrue(QiblaCompassView.isNearToDegree(182f, 180f))
        assertFalse(QiblaCompassView.isNearToDegree(183f, 180f))
        assertFalse(QiblaCompassView.isNearToDegree(184f, 180f))
    }

    @Test
    fun test_it_different_date_object_equal() {
        assertFalse(CivilDate(2000, 1, 1) == PersianDate(2000, 1, 1))
        assertTrue(CivilDate(2000, 1, 1) == CivilDate(2000, 1, 1))
        assertFalse(CivilDate(2000, 1, 1) == CivilDate(2000, 2, 1))
    }

    @Test
    fun tests_imported_from_calendariale() {
        val J0000 = 1721425L // Ours is different apparently
        arrayOf(
//        arrayOf(-214193, -1208, 5, 1),
//        arrayOf(-61387, -790, 9, 14),
//        arrayOf(25469, -552, 7, 2),
//        arrayOf(49217, -487, 7, 9),
//        arrayOf(171307, -153, 10, 18),
//        arrayOf(210155, -46, 2, 30),
                arrayOf(253427, 73, 8, 19),
                arrayOf(369740, 392, 2, 5),
                arrayOf(400085, 475, 3, 3),
                arrayOf(434355, 569, 1, 3),
                arrayOf(452605, 618, 12, 20),
                arrayOf(470160, 667, 1, 14),
                arrayOf(473837, 677, 2, 8),
                arrayOf(507850, 770, 3, 22),
                arrayOf(524156, 814, 11, 13),
                arrayOf(544676, 871, 1, 21),
                arrayOf(567118, 932, 6, 28),
                arrayOf(569477, 938, 12, 14),
                arrayOf(601716, 1027, 3, 21),
                arrayOf(613424, 1059, 4, 10),
                arrayOf(626596, 1095, 5, 2),
                arrayOf(645554, 1147, 3, 30),
                arrayOf(664224, 1198, 5, 10),
                arrayOf(671401, 1218, 1, 7),
                arrayOf(694799, 1282, 1, 29),
                arrayOf(704424, 1308, 6, 3),
                arrayOf(708842, 1320, 7, 7),
                arrayOf(709409, 1322, 1, 29),
                arrayOf(709580, 1322, 7, 14),
                arrayOf(727274, 1370, 12, 27),
                arrayOf(728714, 1374, 12, 6),
                arrayOf(744313, 1417, 8, 19),
                arrayOf(764652, 1473, 4, 28)
        ).forEach {
            assertEquals(it[0] + J0000, PersianDate(it[1], it[2], it[3]).toJdn())
            val from = PersianDate(it[0] + J0000)
            assertEquals(from.year, it[1])
            assertEquals(from.month, it[2])
            assertEquals(from.dayOfMonth, it[3])
        }

        arrayOf(
//        arrayOf(1507231, -586, 7, 24),
//        arrayOf(1660037, -168, 12, 5),
//        arrayOf(1746893, 70, 9, 24),
//        arrayOf(1770641, 135, 10, 2),
//        arrayOf(1892731, 470, 1, 8),
//        arrayOf(1931579, 576, 5, 20),
//        arrayOf(1974851, 694, 11, 10),
//        arrayOf(2091164, 1013, 4, 25),
//        arrayOf(2121509, 1096, 5, 24),
//        arrayOf(2155779, 1190, 3, 23),
//        arrayOf(2174029, 1240, 3, 10),
//        arrayOf(2191584, 1288, 4, 2),
//        arrayOf(2195261, 1298, 4, 27),
//        arrayOf(2229274, 1391, 6, 12),
//        arrayOf(2245580, 1436, 2, 3),
//        arrayOf(2266100, 1492, 4, 9),
//        arrayOf(2288542, 1553, 9, 19),
//        arrayOf(2290901, 1560, 3, 5),
//        arrayOf(2323140, 1648, 6, 10),
                arrayOf(2334848, 1680, 6, 30),
                arrayOf(2348020, 1716, 7, 24),
                arrayOf(2366978, 1768, 6, 19),
                arrayOf(2385648, 1819, 8, 2),
                arrayOf(2392825, 1839, 3, 27),
                arrayOf(2416223, 1903, 4, 19),
                arrayOf(2425848, 1929, 8, 25),
                arrayOf(2430266, 1941, 9, 29),
                arrayOf(2430833, 1943, 4, 19),
                arrayOf(2431004, 1943, 10, 7),
                arrayOf(2448698, 1992, 3, 17),
                arrayOf(2450138, 1996, 2, 25),
                arrayOf(2465737, 2038, 11, 10),
                arrayOf(2486076, 2094, 7, 18)
        ).forEach {
            assertEquals(it[0] + 1L, CivilDate(it[1], it[2], it[3]).toJdn())
            val from = CivilDate(it[0] + 1L)
            assertEquals(from.year, it[1])
            assertEquals(from.month, it[2])
            assertEquals(from.dayOfMonth, it[3])
        }

        arrayOf(
//        arrayOf(-214193, -1245, 12, 11),
//        arrayOf(-61387, -813, 2, 25),
//        arrayOf(25469, -568, 4, 2),
//        arrayOf(49217, -501, 4, 7),
//        arrayOf(171307, -157, 10, 18),
//        arrayOf(210155, -47, 6, 3),
//        arrayOf(253427, 75, 7, 13),
//        arrayOf(369740, 403, 10, 5),
//        arrayOf(400085, 489, 5, 22),
//        arrayOf(434355, 586, 2, 7),
                arrayOf(452605, 637, 8, 7),
//        arrayOf(470160, 687, 2, 21),
//        arrayOf(473837, 697, 7, 7),
//        arrayOf(507850, 793, 6, 30),
//        arrayOf(524156, 839, 7, 6),
//        arrayOf(544676, 897, 6, 2),
//        arrayOf(567118, 960, 9, 30),
//        arrayOf(569477, 967, 5, 27),
//        arrayOf(601716, 1058, 5, 18),
//        arrayOf(613424, 1091, 6, 3),
//        arrayOf(626596, 1128, 8, 4),
                arrayOf(645554, 1182, 2, 4),
//        arrayOf(664224, 1234, 10, 10),
//        arrayOf(671401, 1255, 1, 11),
//        arrayOf(694799, 1321, 1, 20),
                arrayOf(704424, 1348, 3, 19),
//        arrayOf(708842, 1360, 9, 7),
//        arrayOf(709409, 1362, 4, 14),
//        arrayOf(709580, 1362, 10, 7),
//        arrayOf(727274, 1412, 9, 12),
//        arrayOf(728714, 1416, 10, 5),
//        arrayOf(744313, 1460, 10, 12),
                arrayOf(764652, 1518, 3, 5)
        ).forEach {
            assertEquals("${it[0]}", it[0] + J0000, IslamicDate(it[1], it[2], it[3]).toJdn())
            val from = IslamicDate(it[0] + J0000)
            assertEquals(from.year, it[1])
            assertEquals(from.month, it[2])
            assertEquals(from.dayOfMonth, it[3])
        }
    }

    @Test
    fun test_leap_years() {
        // Doesn't match with https://calendar.ut.ac.ir/Fa/News/Data/Doc/KabiseShamsi1206-1498.pdf
        val leapYears = arrayListOf(
                1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243, 1247, 1251, 1255, 1259, 1263,
                1267, 1271, 1276, 1280, 1284, 1288, 1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321,
                1325, 1329, 1333, 1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
                1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416, 1420, 1424, 1428, 1432, 1436,
                1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469, 1474, 1478, 1482, 1486, 1490, 1494,
                1498)

        (1206..1498).forEach {
            assertEquals(it.toString(), if (leapYears.contains(it)) 30 else 29,
                    Utils.getMonthLength(CalendarType.SHAMSI, it, 12))
        }
    }

    @Test
    fun test_equinox_time() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))

        arrayOf(
                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201398-Full.pdf
                arrayOf(2019, 3, 21, 1, 28, 13/*should be 27*/),

                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201397-Full.pdf
                arrayOf(2018, 3, 20, 19, 45, 53/*should be 28*/),

                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201396-Full.pdf
                arrayOf(2017, 3, 20, 13, 59/*should be 58*/, 38/*should be 40*/),

                // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201395-Full.pdf
                arrayOf(2016, 3, 20, 8, 0, 55/*should be 12*/),

                // http://vetmed.uk.ac.ir/documents/203998/204600/calendar-1394.pdf
                arrayOf(2015, 3, 21, 2, 16/*should be 15*/, 0/*should be 11*/),

                // https://raw.githubusercontent.com/ilius/starcal/master/plugins/iran-jalali-data.txt
                arrayOf(2014, 3, 20, 20, 27, 41/*should be 7*/),
                arrayOf(2013, 3, 20, 14, 32/*should be 31*/, 41/*should be 56*/),
                arrayOf(2012, 3, 20, 8, 44, 19/*should be 27*/),
                arrayOf(2011, 3, 21, 2, 51/*should be 50*/, 38/*should be 25*/),
                arrayOf(2010, 3, 20, 21, 2, 49/*should be 13*/),
                arrayOf(2009, 3, 20, 15, 14/*should be 13*/, 50/*should be 39*/),
                arrayOf(2008, 3, 20, 9, 18, 17/*should be 19*/)
        ).forEach {
            calendar.time = Equinox.northwardEquinox(it[0])
            assertEquals(it[0].toString(), it[0], calendar.get(Calendar.YEAR))
            assertEquals(it[0].toString(), it[1], calendar.get(Calendar.MONTH) + 1)
            assertEquals(it[0].toString(), it[2], calendar.get(Calendar.DAY_OF_MONTH))
            assertEquals(it[0].toString(), it[3], calendar.get(Calendar.HOUR_OF_DAY))
            assertEquals(it[0].toString(), it[4], calendar.get(Calendar.MINUTE))
            assertEquals(it[0].toString(), it[5], calendar.get(Calendar.SECOND))
        }

        // And not having random crashes
        (-2000..10000).forEach { Equinox.northwardEquinox(it) }
    }
}
