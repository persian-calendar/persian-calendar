package com.byagowi.persiancalendar.ui

import android.icu.util.ChineseCalendar
import android.os.Build
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

@RunWith(AndroidJUnit4::class)
class ConverterScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun converterScreenSmokeTest() {
        composeTestRule.setContentWithParent {
            ConverterScreen({}, {}, viewModel(), null)
        }
    }

    @Test
    fun converterScreenConverterSmokeTest() {
        composeTestRule.setContentWithParent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.screenMode = ConverterScreenMode.CONVERTER
            ConverterScreen({}, {}, viewModel, null)
        }
    }

    @Test
    fun converterScreenDistanceSmokeTest() {
        composeTestRule.setContentWithParent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.screenMode = ConverterScreenMode.DISTANCE
            ConverterScreen({}, {}, viewModel, null)
        }
    }

    @Test
    fun converterScreenCalculatorSmokeTest() {
        composeTestRule.setContentWithParent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.screenMode = ConverterScreenMode.CALCULATOR
            ConverterScreen({}, {}, viewModel, null)
        }
    }

    @Test
    fun converterScreenQrCodeSmokeTest() {
        composeTestRule.setContentWithParent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.screenMode = ConverterScreenMode.QR_CODE
            ConverterScreen({}, {}, viewModel, null)
        }
    }

    @Test
    fun converterScreenTimeZonesSmokeTest() {
        composeTestRule.setContentWithParent {
            val viewModel = viewModel<ConverterViewModel>()
            viewModel.screenMode = ConverterScreenMode.TIME_ZONES
            ConverterScreen({}, {}, viewModel, null)
        }
    }

    @Test
    fun testTodayButtonVisibilityConverter() {
        val values = mutableListOf<Boolean>()
        val viewModel = ConverterViewModel()
        composeTestRule.setContent {
            LaunchedEffect(viewModel.todayButtonVisibility) {
                values += viewModel.todayButtonVisibility
            }
        }
        listOf(
            { viewModel.selectedDate = Jdn.today() + 1 },
            { viewModel.selectedDate = Jdn.today() },
        ).forEach { it(); composeTestRule.waitForIdle() }
        assertEquals(listOf(false, true, false), values)
    }

    @Test
    fun testTodayButtonVisibility() {
        val values = mutableListOf<Boolean>()
        val viewModel = ConverterViewModel()
        composeTestRule.setContent {
            LaunchedEffect(viewModel.todayButtonVisibility) {
                values += viewModel.todayButtonVisibility
            }
        }
        listOf(
            { viewModel.screenMode = ConverterScreenMode.DISTANCE },
            { viewModel.secondSelectedDate = Jdn.today() + 1 },
            { viewModel.secondSelectedDate = Jdn.today() },
            { viewModel.secondSelectedDate = Jdn.today() + 1 },
            { viewModel.screenMode = ConverterScreenMode.CONVERTER },
            { viewModel.secondSelectedDate = Jdn.today() },
            { viewModel.screenMode = ConverterScreenMode.CALCULATOR },
            { viewModel.secondSelectedDate = Jdn.today() },
            { viewModel.screenMode = ConverterScreenMode.QR_CODE },
            { viewModel.secondSelectedDate = Jdn.today() },
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
