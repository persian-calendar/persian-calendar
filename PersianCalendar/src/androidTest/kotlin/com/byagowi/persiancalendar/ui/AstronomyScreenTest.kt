package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.today
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.map
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test

class AstronomyScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val today = Jdn.today()

    @Test
    fun basicSmokeTest() {
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
        val initialTime = System.currentTimeMillis()
        composeTestRule.setContent {
            mapString = stringResource(Res.string.map)
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
