package com.byagowi.persiancalendar.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.compass.CompassScreen
import org.junit.Rule
import org.junit.Test

class CompassScreenTest {
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
            CompassScreen({}, {}, {})
        }
    }

    @Test
    fun navigateToLevelIsCalled() {
        var levelString = ""
        var navigateToLevelIsCalled = false
        composeTestRule.setContentWithTheme {
            levelString = stringResource(R.string.level)
            CompassScreen(
                openDrawer = {},
                navigateToLevel = { navigateToLevelIsCalled = true },
                navigateToMap = { assert(false) },
            )
        }
        assert(!navigateToLevelIsCalled)
        composeTestRule.onNodeWithContentDescription(levelString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToLevelIsCalled)
    }

    @Test
    fun navigateToMapIsCalled() {
        var mapString = ""
        var navigateToMapIsCalled = false
        composeTestRule.setContentWithTheme {
            mapString = stringResource(R.string.map)
            CompassScreen(
                openDrawer = {},
                navigateToLevel = { assert(false) },
                navigateToMap = { navigateToMapIsCalled = true },
            )
        }
        assert(!navigateToMapIsCalled)
        composeTestRule.onNodeWithContentDescription(mapString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToMapIsCalled)
    }
}
