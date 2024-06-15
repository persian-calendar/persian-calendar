package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalSharedTransitionApi::class)
class AstronomyScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            AstronomyScreen(scope, {}, {}, viewModel())
        }
    }

    @Test
    fun astronomyScreenNavigateToMap() {
        var navigateToMapIsCalled = false
        var mapString = ""
        composeTestRule.setContentWithParent { scope ->
            mapString = stringResource(R.string.map)
            AstronomyScreen(scope, {}, { navigateToMapIsCalled = true }, viewModel())
        }
        composeTestRule.onNodeWithContentDescription(mapString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToMapIsCalled)
    }
}
