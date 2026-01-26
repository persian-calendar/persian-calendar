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
        composeTestRule.setContent {
            NavigationMock {
                SettingsScreen(
                    {},
                    {},
                    SettingsTab.InterfaceCalendar,
                    "",
                    "",
                )
            }
        }
    }

    @Test
    fun bringWidgetNotificationTab() {
        composeTestRule.setContent {
            NavigationMock {
                SettingsScreen(
                    {},
                    {},
                    SettingsTab.WidgetNotification,
                    "",
                    "",
                )
            }
        }
    }

    @Test
    fun bringLocationAthanTab() {
        composeTestRule.setContent {
            NavigationMock {
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
}
