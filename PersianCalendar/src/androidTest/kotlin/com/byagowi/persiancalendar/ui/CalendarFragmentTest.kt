package com.byagowi.persiancalendar.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarFragmentTest {
    @Test
    fun testTodayButtonVisibility() {
        launchFragmentInContainer<CalendarFragment>(themeResId = R.style.LightTheme).onFragment {
            val viewModel by it.viewModels<CalendarViewModel>()
            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibility.collect(values::add) }
                viewModel.selectedDay.value = Jdn.today() + 1
                viewModel.selectedDay.value = Jdn.today()
                viewModel.selectedDay.value = Jdn.today() - 1
                viewModel.selectedDay.value = Jdn.today()
                job.cancel()
                assertEquals(listOf(false, false, true, false, true, false), values)
            }

            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibility.collect(values::add) }
                viewModel.selectedDay.value = Jdn.today() + 1
                viewModel.selectedMonth.value =
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 1)
                viewModel.selectedDay.value = Jdn.today()
                viewModel.selectedMonth.value =
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 0)
                viewModel.selectedMonth.value =
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), -1)
                viewModel.selectedMonth.value =
                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 0)
                job.cancel()
                assertEquals(listOf(false, false, true, true, true, false, true, false), values)
            }
        }
    }
}
