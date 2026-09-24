package com.byagowi.persiancalendar

import io.github.persiancalendar.Equinox
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class EquinoxTests {
    @Test
    fun test_not_change() {
        listOf(
            // Official announced values are from
            // https://github.com/persian-calendar/equinox-research/blob/main/iran-ground-truth.json
            // https://calendar.ut.ac.ir/documents/2139738/7092644/Calendar-1405.pdf
            intArrayOf(2026, 3, 20, 18, 15, 57), // 59 per University of Tehran
            // https://calendar.ut.ac.ir/documents/2139738/7092644/Calendar-1404.pdf
            intArrayOf(2025, 3, 20, 12, 31, 29), // 30 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1403.pdf
            intArrayOf(2024, 3, 20, 6, 36, 24), // 26 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1402.pdf
            intArrayOf(2023, 3, 21, 0, 54, 26), // 28 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1401.pdf
            intArrayOf(2022, 3, 20, 19, 3, 25), // 26 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1400.pdf
            intArrayOf(2021, 3, 20, 13, 7, 28), // 28 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1399.pdf
            intArrayOf(2020, 3, 20, 7, 19, 36), // 37 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1398.pdf
            intArrayOf(2019, 3, 21, 1, 28, 26), // 27 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1397.pdf
            intArrayOf(2018, 3, 20, 19, 45, 27), // 28 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1396.pdf
            intArrayOf(2017, 3, 20, 13, 58, 37), // 40 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1395.pdf
            intArrayOf(2016, 3, 20, 8, 0, 11), // 12 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1394.pdf
            intArrayOf(2015, 3, 21, 2, 15, 9), // 11 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1393.pdf
            intArrayOf(2014, 3, 20, 20, 27, 5), // 7 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1392.pdf
            intArrayOf(2013, 3, 20, 14, 31, 54), // 56 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1391.pdf
            intArrayOf(2012, 3, 20, 8, 44, 25), // 27 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1390.pdf
            intArrayOf(2011, 3, 21, 2, 50, 43), // 45 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1389.pdf
            intArrayOf(2010, 3, 20, 21, 2, 12), // 13 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1388.pdf
            intArrayOf(2009, 3, 20, 15, 13, 37), // 39 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1387.pdf
            intArrayOf(2008, 3, 20, 9, 18, 17), // 19 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1386.pdf
            intArrayOf(2007, 3, 21, 3, 37, 24), // 26 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1385.pdf
            intArrayOf(2006, 3, 20, 21, 55, 33), // 35 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1384.pdf
            intArrayOf(2005, 3, 20, 16, 3, 25), // 24 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1383.pdf
            intArrayOf(2004, 3, 20, 10, 18, 38), // 37 per University of Tehran
            // https://calendar.ut.ac.ir/Fa/Tyear/Data/full-1382.pdf
            intArrayOf(2003, 3, 21, 4, 29, 46), // 45 per University of Tehran
        ).forEach { row ->
            val calendar = GregorianCalendar(TimeZone.getTimeZone(IRAN_TIMEZONE_ID))
            calendar.timeInMillis = Equinox.NORTHWARD_EQUINOX of row[0]
            assertEquals(row[0], calendar[GregorianCalendar.YEAR])
            assertEquals(row[1], calendar[GregorianCalendar.MONTH] + 1)
            assertEquals(row[2], calendar[GregorianCalendar.DAY_OF_MONTH])
            assertEquals(row[3], calendar[GregorianCalendar.HOUR_OF_DAY])
            assertEquals(row[4], calendar[GregorianCalendar.MINUTE])
            assertEquals(row[5], calendar[GregorianCalendar.SECOND])
        }
    }

    @Test
    fun doesNotThrowAcrossExtendedRange() {
        (-2000..10000).forEach {
            Equinox.NORTHWARD_EQUINOX of it
            Equinox.NORTHERN_SOLSTICE of it
            Equinox.SOUTHWARD_EQUINOX of it
            Equinox.SOUTHERN_SOLSTICE of it
        }
    }

    @Test
    fun test_other_equinoxes() {
        assertEquals(1584676176971, Equinox.NORTHWARD_EQUINOX of 2020)
        assertEquals(1592689420448, Equinox.NORTHERN_SOLSTICE of 2020)
        assertEquals(1600781439124, Equinox.SOUTHWARD_EQUINOX of 2020)
        assertEquals(1608544940426, Equinox.SOUTHERN_SOLSTICE of 2020)
    }
}
