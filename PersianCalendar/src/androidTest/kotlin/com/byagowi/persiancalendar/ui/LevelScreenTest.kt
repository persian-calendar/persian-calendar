package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

class LevelScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

//    @Test
//    fun basicSmokeTest() {
//        composeTestRule.setContent { NavigationMock { LevelScreen({}, {}) } }
//    }
//
//    @Test
//    fun navigateUpIsCalled() {
//        var navigateUpString = ""
//        var navigateUpIsCalled = false
//        composeTestRule.setContent {
//            navigateUpString = stringResource(R.string.navigate_up)
//            NavigationMock {
//                LevelScreen(
//                    navigateUp = { navigateUpIsCalled = true },
//                    navigateToCompass = { assert(false) },
//                )
//            }
//        }
//        assert(!navigateUpIsCalled)
//        composeTestRule.onNodeWithContentDescription(navigateUpString)
//            .assertHasClickAction()
//            .performClick()
//        assert(navigateUpIsCalled)
//    }
//
//    @Test
//    fun navigateToCompassIsCalled() {
//        var compassString = ""
//        var navigateToCompassIsCalled = false
//        composeTestRule.setContent {
//            compassString = stringResource(R.string.compass)
//            NavigationMock {
//                LevelScreen(
//                    navigateUp = { assert(false) },
//                    navigateToCompass = { navigateToCompassIsCalled = true },
//                )
//            }
//        }
//        assert(!navigateToCompassIsCalled)
//        composeTestRule.onNodeWithContentDescription(compassString)
//            .assertHasClickAction()
//            .performClick()
//        assert(navigateToCompassIsCalled)
//    }
}
