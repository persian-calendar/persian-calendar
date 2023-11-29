package com.byagowi.persiancalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
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

    // TODO: To get rid of when all the theme system is moved to compose
    private fun ComposeContentTestRule.setContentWithTheme(body: @Composable () -> Unit) {
        setContent {
            val context = LocalContext.current
            context.setTheme(R.style.LightTheme); context.setTheme(R.style.SharedStyle)
            body()
        }
    }

    @Test
    fun aboutScreenNavigateToDeviceInformation() {
        var navigateToDeviceInformationIsCalled = false
        var deviceInformationString = ""
        composeTestRule.setContentWithTheme {
            deviceInformationString = stringResource(R.string.device_information)
            AboutScreen(
                navigateToDeviceInformation = { navigateToDeviceInformationIsCalled = true },
                navigateToLicenses = { assert(false) },
            )
        }
//        composeTestRule.onNodeWithContentDescription(deviceInformationString)
//            .assertHasClickAction()
//            .performClick()
//        assert(navigateToDeviceInformationIsCalled)
    }

    @Test
    fun aboutScreenNavigateToLicenses() {
        var navigateToLicensesIsCalled = false
        var licensesString = ""
        composeTestRule.setContentWithTheme {
            licensesString = stringResource(R.string.about_license_title)
            AboutScreen(
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
        composeTestRule.setContentWithTheme { DeviceInformationScreen {} }
    }

    @Test
    fun licensesSmokeTest() {
        composeTestRule.setContentWithTheme { LicensesScreen {} }
    }
}
