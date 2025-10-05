package com.byagowi.persiancalendar.ui

import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreenMode
import com.byagowi.persiancalendar.ui.converter.ConverterViewModel
import io.github.persiancalendar.calendar.CivilDate
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
            ConverterScreen(scope, {}, {}, viewModel(), null)
        }
    }

    @Test
    fun converterScreenConverterSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.CONVERTER)
            ConverterScreen(scope, {}, {}, viewModel, null)
        }
    }

    @Test
    fun converterScreenDistanceSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.DISTANCE)
            ConverterScreen(scope, {}, {}, viewModel, null)
        }
    }

    @Test
    fun converterScreenCalculatorSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.CALCULATOR)
            ConverterScreen(scope, {}, {}, viewModel, null)
        }
    }

    @Test
    fun converterScreenQrCodeSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.QR_CODE)
            ConverterScreen(scope, {}, {}, viewModel, null)
        }
    }

    @Test
    fun converterScreenTimeZonesSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.changeScreenMode(ConverterScreenMode.TIME_ZONES)
            ConverterScreen(scope, {}, {}, viewModel, null)
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

    @Test
    fun testChineseAnimalYearName() {
        // https://en.wikipedia.org/wiki/Chinese_zodiac#Chinese_calendar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) (1..5).flatMap {
            listOf(
                ChineseZodiac.RAT, ChineseZodiac.OX, ChineseZodiac.TIGER,
                ChineseZodiac.RABBIT, ChineseZodiac.DRAGON, ChineseZodiac.SNAKE,
                ChineseZodiac.HORSE, ChineseZodiac.GOAT, ChineseZodiac.MONKEY,
                ChineseZodiac.ROOSTER, ChineseZodiac.DOG, ChineseZodiac.PIG,
            )
        }.zip(0..<60) { expected, year ->
            run {
                val time = Jdn(CivilDate(year + 1924, 6, 1)).toGregorianCalendar().time
                assertEquals(expected, ChineseZodiac.fromChineseCalendar(ChineseCalendar(time)))
            }
            run {
                val date = ChineseCalendar(year + 1, 0, 0, 1)
                assertEquals(expected, ChineseZodiac.fromChineseCalendar(date))
            }
        }
    }
}
