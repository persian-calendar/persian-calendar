package com.byagowi.persiancalendar.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.about.DeviceInformationScreen
import com.byagowi.persiancalendar.ui.about.LicensesScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Have a look at https://developer.android.com/static/images/jetpack/compose/compose-testing-cheatsheet.pdf
@RunWith(AndroidJUnit4::class)
class AboutScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aboutScreenNavigateToDeviceInformation() {
        var navigateToDeviceInformationIsCalled = false
        var deviceInformationString = ""
        composeTestRule.setContent {
            deviceInformationString = stringResource(R.string.device_information)
            AboutScreen(
                openDrawer = {},
                navigateToDeviceInformation = { navigateToDeviceInformationIsCalled = true },
                navigateToLicenses = { assert(false) },
            )
        }
        composeTestRule.onNodeWithContentDescription(deviceInformationString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToDeviceInformationIsCalled)
    }

    @Test
    fun aboutScreenNavigateToLicenses() {
        var navigateToLicensesIsCalled = false
        var licensesString = ""
        composeTestRule.setContent {
            licensesString = stringResource(R.string.about_license_title)
            AboutScreen(
                openDrawer = {},
                navigateToDeviceInformation = { assert(false) },
                navigateToLicenses = { navigateToLicensesIsCalled = true },
            )
        }
        composeTestRule.onNodeWithText(licensesString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToLicensesIsCalled)
    }

    @Test
    fun deviceInformationSmokeTest() {
        composeTestRule.setContent { DeviceInformationScreen {} }
    }

    @Test
    fun licensesSmokeTest() {
        composeTestRule.setContent { LicensesScreen {} }
    }
}
