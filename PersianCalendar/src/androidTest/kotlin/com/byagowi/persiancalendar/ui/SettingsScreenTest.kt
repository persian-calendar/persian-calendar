package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.junit4.createComposeRule
import com.byagowi.persiancalendar.ui.settings.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.WIDGET_NOTIFICATION_TAB
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalSharedTransitionApi::class)
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            SettingsScreen(scope, {}, {}, 0, "")
        }
    }

    @Test
    fun bringInterfaceCalendarTab() {
        composeTestRule.setContentWithParent { scope ->
            SettingsScreen(scope, {}, {}, INTERFACE_CALENDAR_TAB, "")
        }
    }

    @Test
    fun bringWidgetNotificationTab() {
        composeTestRule.setContentWithParent { scope ->
            SettingsScreen(
                scope,
                {},
                {},
                WIDGET_NOTIFICATION_TAB,
                ""
            )
        }
    }

    @Test
    fun bringLocationAthanTab() {
        composeTestRule.setContentWithParent { scope ->
            SettingsScreen(scope, {}, {}, LOCATION_ATHAN_TAB, "")
        }
    }
}
