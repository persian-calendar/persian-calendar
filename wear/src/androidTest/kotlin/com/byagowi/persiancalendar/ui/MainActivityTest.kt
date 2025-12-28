package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun smokeTest_activityLaunches() {
        composeTestRule.waitForIdle()
    }

    @Test
    fun navigationTest_utilitiesScreen() {
        composeTestRule
            .onNodeWithContentDescription("ابزارها")
            .performClick()

        composeTestRule
            .onNodeWithText("ابزارها")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("مبدل")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("تقویم")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("تنظیمات")
            .assertIsDisplayed()
    }

    @Test
    fun backGesture_navigatesBack_whenOnNestedScreen() {
        composeTestRule
            .onNodeWithText("تنظیمات")
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithContentDescription("ابزارها")
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText("تنظیمات")
            .assertIsDisplayed()
        Espresso.pressBack()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription("ابزارها")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("تنظیمات")
            .assertIsNotDisplayed()
        assertTrue(
            "Activity should not be finishing after back press on nested screen",
            !composeTestRule.activity.isFinishing,
        )
    }

    @Test
    fun backGesture_exitsApp_whenOnRootScreen() {
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription("ابزارها")
            .assertIsDisplayed()

        runCatching {
            Espresso.pressBack()
            composeTestRule.waitForIdle()
            assertTrue(
                "Activity should be finishing after back press on root screen",
                composeTestRule.activity.isFinishing,
            )
        }.onFailure { exception ->
            if (exception !is NoActivityResumedException) {
                fail("Unexpected exception when pressing back on root screen: ${exception.message}")
            }
        }
    }
}
