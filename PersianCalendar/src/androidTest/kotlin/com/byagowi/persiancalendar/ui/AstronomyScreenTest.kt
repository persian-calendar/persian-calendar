package com.byagowi.persiancalendar.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
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

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContent {
            AstronomyScreen({}, {}, AstronomyViewModel())
        }
    }

    @Test
    fun astronomyScreenNavigateToMap() {
        var navigateToMapIsCalled = false
        var mapString = ""
        composeTestRule.setContent {
            mapString = stringResource(R.string.map)
            AstronomyScreen({}, { navigateToMapIsCalled = true }, AstronomyViewModel())
        }
        composeTestRule.onNodeWithContentDescription(mapString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToMapIsCalled)
    }
}
