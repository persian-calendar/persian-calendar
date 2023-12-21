package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreenMode
import com.byagowi.persiancalendar.ui.converter.ConverterViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConverterScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun converterScreenSmokeTest() {
        composeTestRule.setContent { ConverterScreen({}, viewModel()) }
    }

    @Test
    fun converterScreenConverterSmokeTest() {
        composeTestRule.setContent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.Converter)
            ConverterScreen({}, viewModel)
        }
    }

    @Test
    fun converterScreenDistanceSmokeTest() {
        composeTestRule.setContent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.Distance)
            ConverterScreen({}, viewModel)
        }
    }

    @Test
    fun converterScreenCalculatorSmokeTest() {
        composeTestRule.setContent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.Calculator)
            ConverterScreen({}, viewModel)
        }
    }

    @Test
    fun converterScreenQrCodeSmokeTest() {
        composeTestRule.setContent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.QrCode)
            ConverterScreen({}, viewModel)
        }
    }

    @Test
    fun converterScreenTimeZonesSmokeTest() {
        composeTestRule.setContent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.TimeZones)
            ConverterScreen({}, viewModel)
        }
    }

//    @Test
//    fun testTodayButtonVisibility() {
//        launchFragmentInContainer<ConverterFragment>(themeResId = R.style.LightTheme).onFragment {
//            val viewModel by it.viewModels<ConverterViewModel>()
//
//            // Converter
//            runTest(UnconfinedTestDispatcher()) {
//                val values = mutableListOf<Boolean>()
//                val job = launch { viewModel.todayButtonVisibilityEvent.collect(values::add) }
//                viewModel.changeSelectedDate(Jdn.today() + 1)
//                viewModel.changeSelectedDate(Jdn.today())
//                job.cancel()
//                assertEquals(listOf(false, false, false, false, true, false), values)
//            }
//
//            // Day distance
//            runTest(UnconfinedTestDispatcher()) {
//                val values = mutableListOf<Boolean>()
//                val job = launch { viewModel.todayButtonVisibilityEvent.collect(values::add) }
//                viewModel.changeScreenMode(ConverterScreenMode.Distance)
//                viewModel.changeSecondSelectedDate(Jdn.today() + 1)
//                viewModel.changeSecondSelectedDate(Jdn.today())
//                viewModel.changeSecondSelectedDate(Jdn.today() + 1)
//                viewModel.changeScreenMode(ConverterScreenMode.Converter)
//                viewModel.changeSecondSelectedDate(Jdn.today())
//                viewModel.changeScreenMode(ConverterScreenMode.Calculator)
//                viewModel.changeSecondSelectedDate(Jdn.today() + 1)
//                job.cancel()
//                val expected = listOf(
//                    false, false, false, false, false, true, false, true, false, false, false, false
//                )
//                // Disabled for now
//                assertEquals(expected, values)
//            }
//        }
//    }
}
