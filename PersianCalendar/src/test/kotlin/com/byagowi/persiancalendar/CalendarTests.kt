package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.WeekDay
import com.byagowi.persiancalendar.global.initiateMonthNamesForTest
import com.byagowi.persiancalendar.ui.calendar.calendarpager.DayTablePositions
import com.byagowi.persiancalendar.utils.calculateDatePartsDifference
import com.byagowi.persiancalendar.utils.fasliDayName
import com.byagowi.persiancalendar.utils.formatAsSeleucidDate
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.jalaliName
import com.byagowi.persiancalendar.utils.persianDayOfYear
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CalendarTests {
    @ParameterizedTest
    @CsvSource(
        "1397, 1, 31",
        "1397, 2, 31",
        "1397, 3, 31",
        "1397, 4, 31",
        "1397, 5, 31",
        "1397, 6, 31",
        "1397, 7, 30",
        "1397, 8, 30",
        "1397, 9, 30",
        "1397, 10, 30",
        "1397, 11, 30",
        "1397, 12, 29",
    )
    fun `getMonthLength calculating correctness`(year: Int, month: Int, day: Int) {
        assertEquals(day, Calendar.SHAMSI.getMonthLength(year, month))
    }

    @Test
    fun `leap years correctness`() {
        // Matches with https://calendar.ut.ac.ir/Fa/News/Data/Doc/KabiseShamsi1206-1498-new.pdf
        val leapYears = listOf(
            1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243, 1247, 1251, 1255, 1259, 1263,
            1267, 1271, 1276, 1280, 1284, 1288, 1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321,
            1325, 1329, 1333, 1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
            1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416, 1420, 1424, 1428, 1432, 1436,
            1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469, 1474, 1478, 1482, 1486, 1490, 1494,
            1498
        )

        (1206..1498).forEach {
            assertEquals(
                if (it in leapYears) 366 else 365,
                Jdn(PersianDate(it + 1, 1, 1)) - Jdn(PersianDate(it, 1, 1)),
                it.toString()
            )
            if (1303 < it) assertEquals(
                if (it in leapYears) 30 else 29,
                Calendar.SHAMSI.getMonthLength(it, 12),
                it.toString()
            )
        }
    }

    @ParameterizedTest
    @CsvSource(
        "0, 1398, 9, 9",
        "1, 1398, 9, 10",
        "2, 1398, 9, 11",
        "3, 1398, 9, 12",
        "4, 1398, 9, 13",
        "5, 1398, 9, 14",
        "6, 1398, 9, 15",
        "0, 1398, 9, 16",
        "1, 1398, 9, 17",
        "2, 1398, 9, 18",
        "3, 1398, 9, 19",
        "4, 1398, 9, 20",
        "5, 1398, 9, 21",
        "6, 1398, 9, 22",
    )
    fun `weekDay calculations correctness`(
        weekDay: Int, year: Int, month: Int, dayOfMonth: Int
    ) {
        assertEquals(weekDay, Jdn(PersianDate(year, month, dayOfMonth)).weekDay.ordinal)
    }

    @ParameterizedTest
    @CsvSource(
        "1363, 3, 19, 1400, 11, 15, 37, 7, 27",
        "1400, 6, 31, 1400, 8, 1, 0, 1, 1",
        "1363, 11, 15, 1400, 11, 15, 37, 0, 0",
        "1363, 11, 14, 1400, 11, 15, 37, 0, 1",
        "1363, 11, 16, 1400, 11, 15, 36, 11, 29",
        "1400, 7, 15, 1400, 11, 15, 0, 4, 0",
        "1400, 8, 15, 1400, 11, 15, 0, 3, 0",
        "1400, 9, 15, 1400, 11, 15, 0, 2, 0",
        "1400, 10, 15, 1400, 11, 15, 0, 1, 0",
        // Adopted from https://www.ssu.ac.ir/cms/fileadmin/user_upload/Moavenatha/MBehdashti/Gostaresh/pdf/amar/dastor/Mohasebe-Sen.pdf
        // Except we don't have the same result
        "1360, 5, 20, 1379, 6, 10, 19, 0, 21",
        "1360, 5, 20, 1379, 6, 10, 19, 0, 21",
        "1360, 5, 20, 1360, 6, 10, 0, 0, 21",
    )
    fun `test date parts difference`(
        fromYear: Int, fromMonth: Int, fromDay: Int,
        toYear: Int, toMonth: Int, toDay: Int,
        year: Int, month: Int, day: Int,
    ) {
        val lower = PersianDate(fromYear, fromMonth, fromDay)
        val higher = PersianDate(toYear, toMonth, toDay)
        val (y, m, d) = calculateDatePartsDifference(lower, higher, Calendar.SHAMSI)
        assertEquals(day, d)
        assertEquals(month, m)
        assertEquals(year, y)
    }

    @ParameterizedTest
    @CsvSource(
        "1400, 12, 15, 75, 89",
        "1400, 10, 1, 1, 89",
        "1400, 12, 29, 89, 89",
        "1399, 12, 30, 90, 90",
        "1399, 9, 30, 90, 90",
        "1399, 8, 30, 60, 90",
        "1399, 7, 30, 30, 90",
        "1399, 6, 31, 93, 93",
        "1399, 4, 1, 1, 93",
        "1399, 3, 31, 93, 93",
    )
    fun `season passed days`(year: Int, month: Int, day: Int, passedDays: Int, daysCount: Int) {
        val jdn = Jdn(Calendar.SHAMSI, year, month, day)
        val (passedDaysInSeason, totalSeasonDays) = jdn.getPositionInSeason()
        assertEquals(passedDays, passedDaysInSeason)
        assertEquals(daysCount, totalSeasonDays)
    }

    @Test
    fun `historical persian dates`() {
        run {
            // https://commons.wikimedia.org/wiki/File:HablolMatin_1907-12-31.pdf
            val jdn = Jdn(CivilDate(1907, 12, 31))
            assertEquals(
                jdn,
                Jdn(IslamicDate(1325, 11, 25))
            )
            val persianDate = jdn.toPersianDate()
            assertEquals(
                "۱۵ دی جلالی ۸۲۹",
                jalaliName(persianDate.year, persianDayOfYear(persianDate, jdn))
            )
        }
        run {
            // https://w.wiki/DkKf
            val jdn = Jdn(CivilDate(1921, 4, 10))
            assertEquals(
                jdn,
                Jdn(IslamicDate(1339, 8, 1)) // غره means first of it
            )
            // We don't have بزگردی yet if not ever
//            val persianDate = jdn.toPersianDate()
//            assertEquals(
//                "۲۱ فروردین ۸۴۳",
//                jalaliName(persianDate, persianDayOfYear(persianDate, jdn))
//            )
        }
        initiateMonthNamesForTest()
        run {
            // https://commons.wikimedia.org/wiki/File:Moz_4_293.pdf
            // FIXME: Change this to 6 when calendar dependency covers the year
            val jdn = Jdn(IslamicDate(1341, 11, 7))
            assertEquals(jdn.weekDay, WeekDay.FRIDAY)
            val persianDate = jdn.toPersianDate()
            assertEquals(
                "۳۲ جوزا ۱۳۰۲",
                formatDate(persianDate)
            )
        }
    }

    @Test
    fun `historical persian dates smoke test`() {
        (Jdn(PersianDate(1200, 1, 1))..<Jdn(PersianDate(1500, 1, 1))).forEach { jdn ->
            val date = jdn.toPersianDate()
            val name = fasliDayName(
                jdn - Jdn(PersianDate(date.year, 1, 1)) + 1
            )
            val dayOfYear =
                persianDayOfYear(PersianDate(date.year, date.month, date.dayOfMonth), jdn)
            assertEquals(
                expected = name,
                actual = fasliDayName(dayOfYear),
                message = "${date.year}/${date.month}/${date.dayOfMonth}" + " " + name
            )
            jalaliName(jdn.toPersianDate().year, dayOfYear)
            formatAsSeleucidDate(jdn)
        }
    }

    @Test
    fun `seleucid date format`() {
        listOf(
            PersianDate(1404, 1, 1) to "۸ آذر ۲۳۳۶",
            PersianDate(1404, 1, 11) to "۱۸ آذر ۲۳۳۶",
            PersianDate(1404, 1, 26) to "۲ نیسان ۲۳۳۶",
            PersianDate(1404, 2, 9) to "۱۶ نیسان ۲۳۳۶",
            PersianDate(1404, 2, 24) to "۱ ایار ۲۳۳۶",
            PersianDate(1404, 3, 7) to "۱۵ ایار ۲۳۳۶",
            PersianDate(1404, 3, 22) to "۳۰ ایار ۲۳۳۶",
            PersianDate(1404, 4, 6) to "۱۴ حزیران ۲۳۳۶",
            PersianDate(1404, 4, 21) to "۲۹ حزیران ۲۳۳۶",
            PersianDate(1404, 5, 4) to "۱۳ تموز ۲۳۳۶",
            PersianDate(1404, 5, 19) to "۲۸ تموز ۲۳۳۶",
            PersianDate(1404, 6, 3) to "۱۲ آب ۲۳۳۶",
            PersianDate(1404, 6, 18) to "۲۷ آب ۲۳۳۶",
            PersianDate(1404, 7, 2) to "۱۱ ایلول ۲۳۳۶",
            PersianDate(1404, 7, 17) to "۲۶ ایلول ۲۳۳۶",
            PersianDate(1404, 8, 1) to "۱۰ تشرین اول ۲۳۳۷",
            PersianDate(1404, 8, 16) to "۲۵ تشرین اول ۲۳۳۷",
            PersianDate(1404, 9, 1) to "۹ تشرین آخر ۲۳۳۷",
            PersianDate(1404, 9, 16) to "۲۴ تشرین آخر ۲۳۳۷",
            PersianDate(1404, 10, 1) to "۹ کانون اول ۲۳۳۷",
            PersianDate(1404, 10, 16) to "۲۴ کانون اول ۲۳۳۷",
            PersianDate(1404, 11, 1) to "۸ کانون آخر ۲۳۳۷",
            PersianDate(1404, 11, 16) to "۲۳ کانون آخر ۲۳۳۷",
            PersianDate(1404, 11, 30) to "۶ شباط ۲۳۳۷",
            PersianDate(1404, 12, 15) to "۲۱ شباط ۲۳۳۷",
        ).map { (persianDate, expected) ->
            { assertEquals(expected, formatAsSeleucidDate(Jdn(persianDate))) }
        }.run(::assertAll)
    }

    @Test
    fun `one day`() {
        val positions = DayTablePositions()
        positions.add(5, 3)

        val indices = mutableListOf<Pair<Int, Int>>()
        positions.forEach { row, column -> indices += row to column }

        assertEquals(listOf(5 to 3), indices)
    }

    @Test
    fun `multiple day`() {
        val positions = DayTablePositions()
        positions.add(0, 0)
        positions.add(5, 2)
        positions.add(5, 6)

        val indices = mutableListOf<Pair<Int, Int>>()
        positions.forEach { row, column -> indices += row to column }

        assertEquals(listOf(0 to 0, 5 to 2, 5 to 6), indices)
    }

    @Test
    fun `empty set`() {
        val positions = DayTablePositions()

        var called = false
        positions.forEach { _, _ -> called = true }

        assertFalse(called)
    }

    @Test
    fun `idempotent for same day`() {
        val positions = DayTablePositions()
        positions.add(5, 6)
        positions.add(5, 6)

        val indices = mutableListOf<Pair<Int, Int>>()
        positions.forEach { row, column -> indices += row to column }

        assertEquals(listOf(5 to 6), indices)
    }

    @Test
    fun `forEach should iterate in ascending order`() {
        val positions = DayTablePositions()
        positions.add(6, 3)
        positions.add(3, 6)
        positions.add(2, 2)

        val indices = mutableListOf<Pair<Int, Int>>()
        positions.forEach { row, column -> indices += row to column }

        assertEquals(listOf(2 to 2, 3 to 6, 6 to 3), indices)
    }
}
