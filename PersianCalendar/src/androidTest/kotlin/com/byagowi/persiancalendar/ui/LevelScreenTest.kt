package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.compass
import com.byagowi.persiancalendar.shared.generated.resources.navigate_up
import com.byagowi.persiancalendar.ui.level.LevelScreen
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test

class LevelScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContent { NavigationMock { LevelScreen({}, {}) } }
    }

    @Test
    fun navigateUpIsCalled() {
        var navigateUpString = ""
        var navigateUpIsCalled = false
        composeTestRule.setContent {
            navigateUpString = stringResource(Res.string.navigate_up)
            NavigationMock {
                LevelScreen(
                    navigateUp = { navigateUpIsCalled = true },
                    navigateToCompass = { assert(false) },
                )
            }
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
        composeTestRule.setContent {
            compassString = stringResource(Res.string.compass)
            NavigationMock {
                LevelScreen(
                    navigateUp = { assert(false) },
                    navigateToCompass = { navigateToCompassIsCalled = true },
                )
            }
        }
        assert(!navigateToCompassIsCalled)
        composeTestRule.onNodeWithContentDescription(compassString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToCompassIsCalled)
    }
}
