package com.byagowi.persiancalendar

import io.github.persiancalendar.Equinox
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class EquinoxTests {
    @Test
    fun `equinox calculations correctness`() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))

        listOf(
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1401.pdf
            listOf(2022, 3, 20, 19, 3, 2/* should be 26*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1400.pdf
            listOf(2021, 3, 20, 13, 7, 14/* should be 28*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1399.pdf
            listOf(2020, 3, 20, 7, 19, 43/* should be 37*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1398.pdf
            listOf(2019, 3, 21, 1, 28, 13/*should be 27*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1397.pdf
            listOf(2018, 3, 20, 19, 44/*should be 45*/, 52/*should be 28*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1396.pdf
            listOf(2017, 3, 20, 13, 58, 38/*should be 40*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1395.pdf
            listOf(2016, 3, 20, 7/*should be 8*/, 59/*should be 0*/, 54/*should be 12*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1394.pdf
            listOf(2015, 3, 21, 2, 14/*should be 15*/, 59/*should be 11*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1393.pdf
            listOf(2014, 3, 20, 20, 26, 41/*should be 7*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1392.pdf
            listOf(2013, 3, 20, 14, 31, 41/*should be 56*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1391.pdf
            listOf(2012, 3, 20, 8, 44, 18/*should be 27*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1390.pdf
            listOf(2011, 3, 21, 2, 50, 37/*should be 25*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1389.pdf
            listOf(2010, 3, 20, 21, 1/*should be 2*/, 49/*should be 13*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1388.pdf
            listOf(2009, 3, 20, 15, 13, 50/*should be 39*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1387.pdf
            listOf(2008, 3, 20, 9, 18, 17/*should be 19*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1386.pdf
            listOf(2007, 3, 21, 3, 37, 15/*should be 26*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1385.pdf
            listOf(2006, 3, 20, 21, 55, 18/*should be 35*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1384.pdf
            listOf(2005, 3, 20, 16, 3, 37/*should be 24*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1383.pdf
            listOf(2004, 3, 20, 10, 18, 41/*should be 37*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1382.pdf
            listOf(2003, 3, 21, 4, 30/*should be 29*/, 21/*should be 45*/),
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1381.pdf
            listOf(2002, 3, 20, 22, 46, 12/*should be 2*/)
        ).forEach {
            calendar.time = Equinox.northwardEquinox(it[0])
            assertEquals(it[0].toString(), it[0], calendar[Calendar.YEAR])
            assertEquals(it[0].toString(), it[1], calendar[Calendar.MONTH] + 1)
            assertEquals(it[0].toString(), it[2], calendar[Calendar.DAY_OF_MONTH])
            assertEquals(it[0].toString(), it[3], calendar[Calendar.HOUR_OF_DAY])
            assertEquals(it[0].toString(), it[4], calendar[Calendar.MINUTE])
            assertEquals(it[0].toString(), it[5], calendar[Calendar.SECOND])
        }

        // And not having random crashes
        (-2000..10000).forEach { Equinox.northwardEquinox(it) }
    }
}
