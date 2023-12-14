package com.byagowi.persiancalendar.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.settings.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.WIDGET_NOTIFICATION_TAB
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // TODO: To get rid of when all the theme system is moved to compose
    private fun ComposeContentTestRule.setContentWithTheme(body: @Composable () -> Unit) {
        setContent {
            val context = LocalContext.current
            context.setTheme(R.style.LightTheme); context.setTheme(R.style.SharedStyle)
            body()
        }
    }

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContentWithTheme {
            SettingsScreen({}, LocalContext.current as ComponentActivity, 0, "")
        }
    }

    @Test
    fun bringInterfaceCalendarTab() {
        composeTestRule.setContentWithTheme {
            SettingsScreen({}, LocalContext.current as ComponentActivity, INTERFACE_CALENDAR_TAB, "")
        }
    }

    @Test
    fun bringWidgetNotificationTab() {
        composeTestRule.setContentWithTheme {
            SettingsScreen(
                {},
                LocalContext.current as ComponentActivity,
                WIDGET_NOTIFICATION_TAB,
                ""
            )
        }
    }

    @Test
    fun bringLocationAthanTab() {
        composeTestRule.setContentWithTheme {
            SettingsScreen({}, LocalContext.current as ComponentActivity, LOCATION_ATHAN_TAB, "")
        }
    }
}
