package io.github.persiancalendar.calendar.persian

import io.github.persiancalendar.calendar.DateTriplet
import io.github.persiancalendar.calendar.PersianDate.Companion.daysInPreviousMonths
import io.github.persiancalendar.calendar.PersianDate.Companion.monthFromDaysCount

// A simple and quick implementation just to be compatible with
// https://calendar.ut.ac.ir/Fa/News/Data/Doc/KabiseShamsi1206-1498-new.pdf
// For a correct implementation accurate for ~9k years, have a look at AlgorithmicConverter
// which matches also with the numbers we have here for IRST longitude.
internal object LookupTableConverter {
    private const val startingYear = 1304
    private val yearsStartingJdn = LongArray(1498 - startingYear)

    init {
        val leapYears = intArrayOf(
            // 1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243, 1247, 1251, 1255, 1259, 1263,
            // 1267, 1271, 1276, 1280, 1284, 1288, 1292, 1296, 1300,
            1304, 1309, 1313, 1317, 1321, 1325, 1329, 1333, 1337, 1342, 1346, 1350, 1354, 1358,
            1362, 1366, 1370, 1375, 1379, 1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416,
            1420, 1424, 1428, 1432, 1436, 1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469, 1474,
            1478, 1482, 1486, 1490, 1494, 1498
        )
        yearsStartingJdn[0] = 2424231 /* jdn of 1304 */
        var i = 0
        var j = 0
        while (i < yearsStartingJdn.size - 1) {
            val year = i + startingYear
            yearsStartingJdn[i + 1] = yearsStartingJdn[i] + if (leapYears[j] == year) 366 else 365
            if (year >= leapYears[j] && j + 1 < leapYears.size) j++
            ++i
        }
    }

    fun toJdn(year: Int, month: Int, day: Int): Long {
        if (year < startingYear || year > startingYear + yearsStartingJdn.size - 1) return -1
        return yearsStartingJdn[year - startingYear] + daysInPreviousMonths(month) + day - 1
    }

    fun fromJdn(jdn: Long): DateTriplet? {
        if (jdn < yearsStartingJdn[0] || jdn > yearsStartingJdn[yearsStartingJdn.size - 1]) return null
        var year = (jdn - yearsStartingJdn[0]).toInt() / 366
        while (year < yearsStartingJdn.size - 1) {
            if (jdn < yearsStartingJdn[year + 1]) break
            ++year
        }
        val startOfYearJdn = yearsStartingJdn[year]
        year += startingYear
        val dayOfYear = (jdn - startOfYearJdn).toInt() + 1
        val month = monthFromDaysCount(dayOfYear)
        val day = dayOfYear - daysInPreviousMonths(month)
        return DateTriplet(year = year, month = month, dayOfMonth = day)
    }
}
