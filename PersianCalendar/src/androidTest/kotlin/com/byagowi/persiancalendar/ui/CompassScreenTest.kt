package com.byagowi.persiancalendar.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.compass.CompassScreen
import org.junit.Rule
import org.junit.Test

class CompassScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContent {
            NavigationMock {
                val now = System.currentTimeMillis()
                CompassScreen({}, {}, {}, {}, null, Jdn.today(), now)
            }
        }
    }

    @Test
    fun navigateToLevelIsCalled() {
        var levelString = ""
        var navigateToLevelIsCalled = false
        composeTestRule.setContent {
            levelString = stringResource(R.string.level)
            NavigationMock {
                CompassScreen(
                    openNavigationRail = {},
                    navigateToLevel = { navigateToLevelIsCalled = true },
                    navigateToMap = { assert(false) },
                    navigateToSettingsLocationTab = {},
                    noBackStackAction = null,
                    today = Jdn.today(),
                    now = System.currentTimeMillis(),
                )
            }
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
        composeTestRule.setContent {
            mapString = stringResource(R.string.map)
            NavigationMock {
                CompassScreen(
                    openNavigationRail = {},
                    navigateToLevel = { assert(false) },
                    navigateToMap = { navigateToMapIsCalled = true },
                    navigateToSettingsLocationTab = {},
                    noBackStackAction = null,
                    today = Jdn.today(),
                    now = System.currentTimeMillis(),
                )
            }
        }
        assert(!navigateToMapIsCalled)
        composeTestRule.onNodeWithContentDescription(mapString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToMapIsCalled)
    }
}
