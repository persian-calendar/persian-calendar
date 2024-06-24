package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.level.LevelScreen
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalSharedTransitionApi::class)
class LevelScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            LevelScreen({}, {}, scope)
        }
    }

    @Test
    fun navigateUpIsCalled() {
        var navigateUpString = ""
        var navigateUpIsCalled = false
        composeTestRule.setContentWithParent { scope ->
            navigateUpString = stringResource(R.string.navigate_up)
            LevelScreen(
                navigateUp = { navigateUpIsCalled = true },
                navigateToCompass = { assert(false) },
                animatedContentScope = scope,
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
        composeTestRule.setContentWithParent { scope ->
            compassString = stringResource(R.string.compass)
            LevelScreen(
                navigateUp = { assert(false) },
                navigateToCompass = { navigateToCompassIsCalled = true },
                animatedContentScope = scope,
            )
        }
        assert(!navigateToCompassIsCalled)
        composeTestRule.onNodeWithContentDescription(compassString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToCompassIsCalled)
    }
}
