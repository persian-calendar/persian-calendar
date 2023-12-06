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
import com.byagowi.persiancalendar.ui.level.LevelScreen
import org.junit.Rule
import org.junit.Test

class LevelScreenTest {
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
            LevelScreen(LocalContext.current as ComponentActivity, {}, {})
        }
    }
    @Test
    fun navigateUpIsCalled() {
        var navigateUpString = ""
        var navigateUpIsCalled = false
        composeTestRule.setContentWithTheme {
            navigateUpString = stringResource(R.string.navigate_up)
            LevelScreen(
                activity = LocalContext.current as ComponentActivity,
                popNavigation = { navigateUpIsCalled = true },
                navigateToCompass = { assert(false) }
            )
        }
        assert(!navigateUpIsCalled)
        composeTestRule.onNodeWithContentDescription(navigateUpString)
            .assertHasClickAction()
            .performClick()
        assert(navigateUpIsCalled)
    }

    @Test
    fun navigateToCompassIsCalled() {
        var compassString = ""
        var navigateToCompassIsCalled = false
        composeTestRule.setContentWithTheme {
            compassString = stringResource(R.string.compass)
            LevelScreen(
                activity = LocalContext.current as ComponentActivity,
                popNavigation = { assert(false) },
                navigateToCompass = { navigateToCompassIsCalled = true }
            )
        }
        assert(!navigateToCompassIsCalled)
        composeTestRule.onNodeWithContentDescription(compassString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToCompassIsCalled)
    }
}
