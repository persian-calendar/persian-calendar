package com.byagowi.persiancalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // TODO: To get rid of when all the theme system is moved to compose
    private fun ComposeContentTestRule.setContentWithTheme(body: @Composable () -> Unit) {
        setContent {
            val context = LocalContext.current
            context.setTheme(R.style.LightTheme); context.setTheme(R.style.SharedStyle)
            body()
        }
    }

    @Test
    fun calendarScreenSmokeTest() {
        composeTestRule.setContentWithTheme { CalendarScreen({}, {}, {}, {}, viewModel()) }
    }

//    @Test
//    fun testTodayButtonVisibility() {
//        launchFragmentInContainer<CalendarFragment>(themeResId = R.style.LightTheme).onFragment {
//            val viewModel by it.viewModels<CalendarViewModel>()
//            runTest(UnconfinedTestDispatcher()) {
//                val values = mutableListOf<Boolean>()
//                val job = launch { viewModel.todayButtonVisibilityEvent.collect(values::add) }
//                viewModel.changeSelectedDay(Jdn.today() + 1)
//                viewModel.changeSelectedDay(Jdn.today())
//                viewModel.changeSelectedDay(Jdn.today() - 1)
//                viewModel.changeSelectedDay(Jdn.today())
//                job.cancel()
//                assertEquals(listOf(false, true, false, true, false), values)
//            }
//
//            runTest(UnconfinedTestDispatcher()) {
//                val values = mutableListOf<Boolean>()
//                val job = launch { viewModel.todayButtonVisibilityEvent.collect(values::add) }
//                viewModel.changeSelectedDay(Jdn.today() + 1)
//                viewModel.changeSelectedMonth(
//                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 1)
//                )
//                viewModel.changeSelectedDay(Jdn.today())
//                viewModel.changeSelectedMonth(
//                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 0)
//                )
//                viewModel.changeSelectedMonth(
//                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), -1)
//                )
//                viewModel.changeSelectedMonth(
//                    mainCalendar.getMonthStartFromMonthsDistance(Jdn.today(), 0)
//                )
//                job.cancel()
//                assertEquals(listOf(false, true, true, true, false, true, false), values)
//            }
//        }
//    }
}
