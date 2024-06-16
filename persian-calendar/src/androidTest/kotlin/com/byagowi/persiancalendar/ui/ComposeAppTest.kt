package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ComposeAppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContent {
            App(null) {}
        }
    }
}
