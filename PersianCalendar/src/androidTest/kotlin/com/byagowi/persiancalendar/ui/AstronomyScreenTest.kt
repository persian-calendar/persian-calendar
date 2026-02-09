package com.byagowi.persiancalendar.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import org.junit.Rule
import org.junit.Test

class AstronomyScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        val today = Jdn.today()
        val initialTime = System.currentTimeMillis()
        composeTestRule.setContent {
            NavigationMock {
                AstronomyScreen({}, {}, initialTime, today, null)
            }
        }
    }

    @Test
    fun astronomyScreenNavigateToMap() {
        var navigateToMapIsCalled = false
        var mapString = ""
        val today = Jdn.today()
        val initialTime = System.currentTimeMillis()
        composeTestRule.setContent {
            mapString = stringResource(R.string.map)
            NavigationMock {
                AstronomyScreen({}, { navigateToMapIsCalled = true }, initialTime, today, null)
            }
        }
        composeTestRule.onNodeWithContentDescription(mapString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToMapIsCalled)
    }
}
