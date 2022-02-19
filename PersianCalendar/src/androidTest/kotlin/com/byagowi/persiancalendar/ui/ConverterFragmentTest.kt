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
            assertEquals(viewModel.jdn.value, Jdn.today())
            assertFalse(viewModel.todayButtonVisibility.value)
            viewModel.jdn.value = Jdn.today() + 1
            assertEquals(viewModel.jdn.value, Jdn.today() + 1)
            assertTrue(viewModel.todayButtonVisibility.value)
            viewModel.jdn.value = Jdn.today()
            assertFalse(viewModel.todayButtonVisibility.value)

            // Day distance
            assertFalse(viewModel.isDayDistance.value)
            viewModel.isDayDistance.value = true
            assertTrue(viewModel.isDayDistance.value)
            viewModel.distanceJdn.value = Jdn.today() + 1
            assertTrue(viewModel.todayButtonVisibility.value)
            viewModel.distanceJdn.value = Jdn.today()

            runTest(UnconfinedTestDispatcher()) {
                val values = mutableListOf<Boolean>()
                val job = launch { viewModel.todayButtonVisibility.collect(values::add) }
                viewModel.distanceJdn.value = Jdn.today() + 1
                job.cancel()
                assertEquals(listOf(false, true), values)
            }
        }
    }
}
