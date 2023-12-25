package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.junit4.createComposeRule
import com.byagowi.persiancalendar.ui.settings.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.WIDGET_NOTIFICATION_TAB
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContent {
            SettingsScreen({}, {}, 0, "")
        }
    }

    @Test
    fun bringInterfaceCalendarTab() {
        composeTestRule.setContent {
            SettingsScreen({}, {}, INTERFACE_CALENDAR_TAB, "")
        }
    }

    @Test
    fun bringWidgetNotificationTab() {
        composeTestRule.setContent {
            SettingsScreen(
                {},
                {},
                WIDGET_NOTIFICATION_TAB,
                ""
            )
        }
    }

    @Test
    fun bringLocationAthanTab() {
        composeTestRule.setContent {
            SettingsScreen({}, {}, LOCATION_ATHAN_TAB, "")
        }
    }
}
