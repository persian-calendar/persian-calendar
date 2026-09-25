package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.today
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.level
import com.byagowi.persiancalendar.shared.generated.resources.map
import com.byagowi.persiancalendar.ui.compass.CompassScreen
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test

class CompassScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private val today = Jdn.today()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContent {
            NavigationMock {
                val now = System.currentTimeMillis()
                CompassScreen({}, {}, {}, {}, null, today, now)
            }
        }
    }

    @Test
    fun navigateToLevelIsCalled() {
        var levelString = ""
        var navigateToLevelIsCalled = false
        composeTestRule.setContent {
            levelString = stringResource(Res.string.level)
            NavigationMock {
                CompassScreen(
                    openNavigationRail = {},
                    navigateToLevel = { navigateToLevelIsCalled = true },
                    navigateToMap = { assert(false) },
                    navigateToSettingsLocationTab = {},
                    noBackStackAction = null,
                    today = today,
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
            mapString = stringResource(Res.string.map)
            NavigationMock {
                CompassScreen(
                    openNavigationRail = {},
                    navigateToLevel = { assert(false) },
                    navigateToMap = { navigateToMapIsCalled = true },
                    navigateToSettingsLocationTab = {},
                    noBackStackAction = null,
                    today = today,
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
