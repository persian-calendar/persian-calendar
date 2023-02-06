package com.byagowi.persiancalendar

import io.github.cosinekitty.astronomy.seasons
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.*
import kotlin.test.assertEquals

class EquinoxTests {
    @ParameterizedTest
    @CsvSource(
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1402.pdf
        "2023, 3, 21, 0, 54, 24" /* should be 28*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1401.pdf
        "2022, 3, 20, 19, 3, 24" /* should be 26*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1400.pdf
        "2021, 3, 20, 13, 7, 21" /* should be 28*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1399.pdf
        "2020, 3, 20, 7, 19, 57" /* should be 37*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1398.pdf
        "2019, 3, 21, 1, 28, 19" /*should be 27*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1397.pdf
        "2018, 3, 20, 19, 45, 19" /*should be 28*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1396.pdf
        "2017, 3, 20, 13, 58, 48" /*should be 40*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1395.pdf
        "2016, 3, 20, 8, 0, 4" /*should be 12*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1394.pdf
        "2015, 3, 21, 2, 15, 4" /*should be 11*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1393.pdf
        "2014, 3, 20, 20, 26, 52" /*should be 7*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1392.pdf
        "2013, 3, 20, 14, 31, 41" /*should be 56*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1391.pdf
        "2012, 3, 20, 8, 44, 16" /*should be 27*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1390.pdf
        "2011, 3, 21, 2, 50, 47" /*should be 25*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1389.pdf
        "2010, 3, 20, 21, 1, 59" /*should be 2:13*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1388.pdf
        "2009, 3, 20, 15, 13, 57" /*should be 39*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1387.pdf
        "2008, 3, 20, 9, 18, 13" /*should be 19*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1386.pdf
        "2007, 3, 21, 3, 37, 31" /*should be 26*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1385.pdf
        "2006, 3, 20, 21, 55, 19" /*should be 35*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1384.pdf
        "2005, 3, 20, 16, 3, 36" /*should be 24*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1383.pdf
        "2004, 3, 20, 10, 18, 25" /*should be 37*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1382.pdf
        "2003, 3, 21, 4, 30, 28" /*should be 29:45*/,
        // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1381.pdf
        "2002, 3, 20, 22, 46, 7" /*should be 2*/
    )
    fun test_not_change(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        calendar.timeInMillis = seasons(year).marchEquinox.toMillisecondsSince1970()
        assertEquals(year, calendar[Calendar.YEAR])
        assertEquals(month, calendar[Calendar.MONTH] + 1)
        assertEquals(day, calendar[Calendar.DAY_OF_MONTH])
        assertEquals(hour, calendar[Calendar.HOUR_OF_DAY])
        assertEquals(minute, calendar[Calendar.MINUTE])
        assertEquals(second, calendar[Calendar.SECOND])
    }

    @Test
    fun test_range() {
        // And not having random crashes
        for (i in 1..4000) seasons(i)
    }

    @Test
    fun test_other_equinoxes() {
        val seasons = seasons(2020)
        assertEquals(1584676197177, seasons.marchEquinox.toMillisecondsSince1970())
        assertEquals(1592689413689, seasons.juneSolstice.toMillisecondsSince1970())
        assertEquals(1600781456763, seasons.septemberEquinox.toMillisecondsSince1970())
        assertEquals(1608544960981, seasons.decemberSolstice.toMillisecondsSince1970())
    }
}
