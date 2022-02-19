package com.byagowi.persiancalendar.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.converter.ConverterFragment
import com.byagowi.persiancalendar.ui.converter.ConverterViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConverterFragmentTest {
    @Test
    fun testTodayButtonVisibility() {
        launchFragmentInContainer<ConverterFragment>(themeResId = R.style.LightTheme).onFragment {
            val viewModel by it.viewModels<ConverterViewModel>()

            // Converter
            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibility.collect(values::add) }
                viewModel.selectedDate.value = Jdn.today() + 1
                viewModel.selectedDate.value = Jdn.today()
                job.cancel()
                assertEquals(listOf(false, false, false, true, false), values)
            }

            // Day distance
            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibility.collect(values::add) }
                viewModel.isDayDistance.value = true
                viewModel.secondSelectedDate.value = Jdn.today() + 1
                viewModel.secondSelectedDate.value = Jdn.today()
                viewModel.secondSelectedDate.value = Jdn.today() + 1
                viewModel.isDayDistance.value = false
                viewModel.secondSelectedDate.value = Jdn.today()
                job.cancel()
                val expected = listOf(false, false, false, false, true, false, true, false, false)
                assertEquals(expected, values)
            }
        }
    }
}
