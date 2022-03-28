package com.byagowi.persiancalendar.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreenMode
import com.byagowi.persiancalendar.ui.converter.ConverterViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConverterScreenTest {
    @Test
    fun testTodayButtonVisibility() {
        launchFragmentInContainer<ConverterScreen>(themeResId = R.style.LightTheme).onFragment {
            val viewModel by it.viewModels<ConverterViewModel>()

            // Converter
            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibilityEvent.collect(values::add) }
                viewModel.changeSelectedDate(Jdn.today() + 1)
                viewModel.changeSelectedDate(Jdn.today())
                job.cancel()
                assertEquals(listOf(false, false, false, true, false), values)
            }

            // Day distance
            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibilityEvent.collect(values::add) }
                viewModel.changeScreenMode(ConverterScreenMode.Distance)
                viewModel.changeSecondSelectedDate(Jdn.today() + 1)
                viewModel.changeSecondSelectedDate(Jdn.today())
                viewModel.changeSecondSelectedDate(Jdn.today() + 1)
                viewModel.changeScreenMode(ConverterScreenMode.Converter)
                viewModel.changeSecondSelectedDate(Jdn.today())
                viewModel.changeScreenMode(ConverterScreenMode.Calculator)
                viewModel.changeSecondSelectedDate(Jdn.today() + 1)
                job.cancel()
                val expected = listOf(
                    false, false, false, false, true, false, true, false, false, false, false
                )
                // Disabled for now
                assertEquals(expected, values)
            }
        }
    }
}
