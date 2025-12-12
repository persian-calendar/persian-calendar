package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.junit4.createComposeRule
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.SettingsTab
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bringInterfaceCalendarTab() {
        composeTestRule.setContentWithParent {
            SettingsScreen(
                {},
                {},
                SettingsTab.InterfaceCalendar,
                "",
                "",
            )
        }
    }

    @Test
    fun bringWidgetNotificationTab() {
        composeTestRule.setContentWithParent {
            SettingsScreen(
                {},
                {},
                SettingsTab.WidgetNotification,
                "",
                "",
            )
        }
    }

    @Test
    fun bringLocationAthanTab() {
        composeTestRule.setContentWithParent {
            SettingsScreen(
                {},
                {},
                SettingsTab.LocationAthan,
                "",
                "",
            )
        }
    }
}
