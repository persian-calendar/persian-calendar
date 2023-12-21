package com.byagowi.persiancalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.byagowi.persiancalendar.R
import org.junit.Rule
import org.junit.Test

class ComposeAppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // TODO: To get rid of when all the theme system is moved to compose
    private fun ComposeContentTestRule.setContentWithTheme(body: @Composable () -> Unit) {
        setContent {
            val context = LocalContext.current
            context.setTheme(R.style.LightTheme); context.setTheme(R.style.SharedStyle)
            body()
        }
    }

    @Test
    fun basicSmokeTest() {
        composeTestRule.setContentWithTheme {
            App(null) {}
        }
    }
}
