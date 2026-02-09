package com.byagowi.persiancalendar.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val today = Jdn.today()

    @Test
    fun calendarScreenSmokeTest() {
        val jdn = mutableStateOf<Jdn?>(null)
        composeTestRule.setContent {
            NavigationMock {
                val now = System.currentTimeMillis()
                CalendarScreen(0, {}, jdn, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, today, now)
            }
        }
    }
}
