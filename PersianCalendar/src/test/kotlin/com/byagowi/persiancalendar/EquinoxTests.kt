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
            // https://calendar.ut.ac.ir/Fa/News/Data/Doc/Calendar%201398-Full.pdf
            listOf(2020, 3, 20, 7, 20/*should be 19*/, 43/* should be 37*/),
            listOf(2019, 3, 21, 1, 28, 13/*should be 27*/),
            listOf(2018, 3, 20, 19, 45, 53/*should be 28*/),
            listOf(2017, 3, 20, 13, 59/*should be 58*/, 38/*should be 40*/),
            listOf(2016, 3, 20, 8, 0, 55/*should be 12*/),
            listOf(2015, 3, 21, 2, 16/*should be 15*/, 0/*should be 11*/),
            listOf(2014, 3, 20, 20, 27, 41/*should be 7*/),
            listOf(2013, 3, 20, 14, 32/*should be 31*/, 41/*should be 56*/),
            listOf(2012, 3, 20, 8, 44, 19/*should be 27*/),
            listOf(2011, 3, 21, 2, 51/*should be 50*/, 38/*should be 25*/),
            listOf(2010, 3, 20, 21, 2, 49/*should be 13*/),
            listOf(2009, 3, 20, 15, 14/*should be 13*/, 50/*should be 39*/),
            listOf(2008, 3, 20, 9, 18, 17/*should be 19*/)
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
