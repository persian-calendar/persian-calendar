package com.byagowi.persiancalendar.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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

    @Test
    fun aboutScreenSmokeTest() = composeTestRule.setContent { AboutScreen({}, {}) }

    @Test
    fun deviceInformationSmokeTest() = composeTestRule.setContent { DeviceInformationScreen {} }

    @Test
    fun licensesSmokeTest() = composeTestRule.setContent { LicensesScreen {} }
}
