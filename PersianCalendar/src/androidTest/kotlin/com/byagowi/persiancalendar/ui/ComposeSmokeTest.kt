package com.byagowi.persiancalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.about.DeviceInformationScreen
import com.byagowi.persiancalendar.ui.about.LicensesScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeSmokeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun test(body: @Composable () -> Unit) {
        composeTestRule.setContent {
            // TODO: To get rid of when all the theme system is moved to compose
            val context = LocalContext.current
            context.setTheme(R.style.DynamicDarkTheme); context.setTheme(R.style.SharedStyle)
            body()
        }
    }

    @Test
    fun aboutScreenSmokeTest() = test { AboutScreen({}, {}) }

    @Test
    fun deviceInformationSmokeTest() = test { DeviceInformationScreen {} }

    @Test
    fun licensesSmokeTest() = test { LicensesScreen {} }
}
