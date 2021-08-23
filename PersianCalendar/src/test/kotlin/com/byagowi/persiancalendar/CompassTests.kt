package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.compass.CompassFragment
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.EnabledHolidays
import com.byagowi.persiancalendar.utils.EventsStore
import com.byagowi.persiancalendar.utils.getEvents
import com.byagowi.persiancalendar.utils.getLastWeekDayOfMonth
import com.byagowi.persiancalendar.utils.getMonthLength
import com.byagowi.persiancalendar.utils.irregularCalendarEventsStore
import com.byagowi.persiancalendar.utils.isMoonInScorpio
import com.byagowi.persiancalendar.utils.loadEvents
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.CalculationMethod
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import io.github.persiancalendar.praytimes.PrayTimesCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class CompassTests {
    @Test
    fun `isNearToDegree correctness`() {
        assertTrue(CompassFragment.isNearToDegree(360f, 1f))
        assertTrue(CompassFragment.isNearToDegree(1f, 360f))

        assertTrue(CompassFragment.isNearToDegree(2f, 360f))
        assertFalse(CompassFragment.isNearToDegree(3f, 360f))

        assertTrue(CompassFragment.isNearToDegree(360f, 2f))
        assertFalse(CompassFragment.isNearToDegree(360f, 3f))

        assertTrue(CompassFragment.isNearToDegree(180f, 181f))
        assertTrue(CompassFragment.isNearToDegree(180f, 182f))
        assertFalse(CompassFragment.isNearToDegree(180f, 183f))
        assertFalse(CompassFragment.isNearToDegree(180f, 184f))

        assertTrue(CompassFragment.isNearToDegree(181f, 180f))
        assertTrue(CompassFragment.isNearToDegree(182f, 180f))
        assertFalse(CompassFragment.isNearToDegree(183f, 180f))
        assertFalse(CompassFragment.isNearToDegree(184f, 180f))
    }
}
