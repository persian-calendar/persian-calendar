package com.byagowi.persiancalendar.ui

import android.icu.util.ChineseCalendar
import android.os.Build
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.astronomy.ChineseZodiac
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreenMode
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
        composeTestRule.setContent {
            NavigationMock {
                ConverterScreen({}, {}, null, Jdn.today())
            }
        }
    }

    @Test
    fun converterScreenConverterSmokeTest() {
        composeTestRule.setContent {
            val screenMode = ConverterScreenMode.CONVERTER
            NavigationMock {
                ConverterScreen({}, {}, null, Jdn.today(), screenMode)
            }
        }
    }

    @Test
    fun converterScreenDistanceSmokeTest() {
        composeTestRule.setContent {
            val screenMode = ConverterScreenMode.DISTANCE
            NavigationMock {
                ConverterScreen({}, {}, null, Jdn.today(), screenMode)
            }
        }
    }

    @Test
    fun converterScreenCalculatorSmokeTest() {
        composeTestRule.setContent {
            val screenMode = ConverterScreenMode.CALCULATOR
            NavigationMock {
                ConverterScreen({}, {}, null, Jdn.today(), screenMode)
            }
        }
    }

    @Test
    fun converterScreenQrCodeSmokeTest() {
        composeTestRule.setContent {
            val screenMode = ConverterScreenMode.QR_CODE
            NavigationMock {
                ConverterScreen({}, {}, null, Jdn.today(), screenMode)
            }
        }
    }

    @Test
    fun converterScreenTimeZonesSmokeTest() {
        composeTestRule.setContent {
            val screenMode = ConverterScreenMode.TIME_ZONES
            NavigationMock {
                ConverterScreen({}, {}, null, Jdn.today(), screenMode)
            }
        }
    }

    @Test
    fun converterScreenTimeZonesStateRestoreTest() {
        val restorationTester = StateRestorationTester(composeTestRule)

        restorationTester.setContent {
            NavigationMock {
                val screenMode = ConverterScreenMode.TIME_ZONES
                ConverterScreen({}, {}, null, Jdn.today(), screenMode)
            }
        }

        restorationTester.emulateSavedInstanceStateRestore()
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
