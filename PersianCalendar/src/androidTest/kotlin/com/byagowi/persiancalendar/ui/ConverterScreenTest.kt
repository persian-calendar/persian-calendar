package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreenMode
import com.byagowi.persiancalendar.ui.converter.ConverterViewModel
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class ConverterScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun converterScreenSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            ConverterScreen(scope, {}, viewModel())
        }
    }

    @Test
    fun converterScreenConverterSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.CONVERTER)
            ConverterScreen(scope, {}, viewModel)
        }
    }

    @Test
    fun converterScreenDistanceSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.DISTANCE)
            ConverterScreen(scope, {}, viewModel)
        }
    }

    @Test
    fun converterScreenCalculatorSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.CALCULATOR)
            ConverterScreen(scope, {}, viewModel)
        }
    }

    @Test
    fun converterScreenQrCodeSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.QR_CODE)
            ConverterScreen(scope, {}, viewModel)
        }
    }

    @Test
    fun converterScreenTimeZonesSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.TIME_ZONES)
            ConverterScreen(scope, {}, viewModel)
        }
    }

    @Test
    fun testTodayButtonVisibilityConverter() {
        val values = mutableListOf<Boolean>()
        val viewModel = ConverterViewModel()
        composeTestRule.setContent {
            LaunchedEffect(Unit) { viewModel.todayButtonVisibility.collect(values::add) }
        }
        listOf(
            { viewModel.changeSelectedDate(Jdn.today() + 1) },
            { viewModel.changeSelectedDate(Jdn.today()) },
        ).forEach { it(); composeTestRule.waitForIdle() }
        assertEquals(listOf(false, true, false), values)
    }

    @Test
    fun testTodayButtonVisibility() {
        val values = mutableListOf<Boolean>()
        val viewModel = ConverterViewModel()
        composeTestRule.setContent {
            LaunchedEffect(Unit) { viewModel.todayButtonVisibility.collect(values::add) }
        }
        listOf(
            { viewModel.changeScreenMode(ConverterScreenMode.DISTANCE) },
            { viewModel.changeSecondSelectedDate(Jdn.today() + 1) },
            { viewModel.changeSecondSelectedDate(Jdn.today()) },
            { viewModel.changeSecondSelectedDate(Jdn.today() + 1) },
            { viewModel.changeScreenMode(ConverterScreenMode.CONVERTER) },
            { viewModel.changeSecondSelectedDate(Jdn.today()) },
            { viewModel.changeScreenMode(ConverterScreenMode.CALCULATOR) },
            { viewModel.changeSecondSelectedDate(Jdn.today()) },
            { viewModel.changeScreenMode(ConverterScreenMode.QR_CODE) },
            { viewModel.changeSecondSelectedDate(Jdn.today()) },
        ).forEach { it(); composeTestRule.waitForIdle() }
        assertEquals(listOf(false, true, false, true, false), values)
    }
}
