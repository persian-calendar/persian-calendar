package com.byagowi.persiancalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import org.junit.Rule
import org.junit.Test

class AstronomyScreenTest {
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
            AstronomyScreen(AstronomyViewModel()) {}
        }
    }

    @Test
    fun astronomyScreenNavigateToMap() {
        var navigateToMapIsCalled = false
        var mapString = ""
        composeTestRule.setContentWithTheme {
            mapString = stringResource(R.string.map)
            AstronomyScreen(AstronomyViewModel()) { navigateToMapIsCalled = true }
        }
        composeTestRule.onNodeWithContentDescription(mapString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToMapIsCalled)
    }
}
