package com.byagowi.persiancalendar.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {
    @Test
    fun testTodayButtonVisibility() {
        launchFragmentInContainer<CalendarScreen>(themeResId = R.style.LightTheme).onFragment {
            val viewModel by it.viewModels<CalendarViewModel>()
            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibilityEvent.collect(values::add) }
                viewModel.changeSelectedDay(Jdn.today() + 1)
                viewModel.changeSelectedDay(Jdn.today())
                viewModel.changeSelectedDay(Jdn.today() - 1)
                viewModel.changeSelectedDay(Jdn.today())
                job.cancel()
                assertEquals(listOf(false, true, false, true, false), values)
            }

            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibilityEvent.collect(values::add) }
                viewModel.changeSelectedDay(Jdn.today() + 1)
                viewModel.changeSelectedMonth(
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 1)
                )
                viewModel.changeSelectedDay(Jdn.today())
                viewModel.changeSelectedMonth(
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 0)
                )
                viewModel.changeSelectedMonth(
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), -1)
                )
                viewModel.changeSelectedMonth(
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 0)
                )
                job.cancel()
                assertEquals(listOf(false, true, true, true, false, true, false), values)
            }
        }
    }
}
