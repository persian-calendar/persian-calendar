package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
            .onNodeWithContentDescription("تنظیمات")
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
}

