package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.junit4.createComposeRule
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.SettingsTab
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalSharedTransitionApi::class)
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bringInterfaceCalendarTab() {
        composeTestRule.setContentWithParent { scope ->
            SettingsScreen(
                scope,
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
        composeTestRule.setContentWithParent { scope ->
            SettingsScreen(
                scope,
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
        composeTestRule.setContentWithParent { scope ->
            SettingsScreen(
                scope,
                {},
                {},
                SettingsTab.LocationAthan,
                "",
                "",
            )
        }
    }
}
