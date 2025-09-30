package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.about.DeviceInformationScreen
import com.byagowi.persiancalendar.ui.about.LicensesScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class AboutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContentWithTheme(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }

    @Test
    fun aboutScreenNavigateToDeviceInformation() {
        var navigateToDeviceInformationIsCalled = false
        setContentWithTheme {
            AboutScreen(
                animatedContentScope = it,
                openDrawer = {},
                navigateToDeviceInformation = { navigateToDeviceInformationIsCalled = true },
                navigateToLicenses = { assert(false) },
            )
        }
        composeTestRule.onNodeWithContentDescription(stringResource(R.string.device_information))
            .assertHasClickAction()
            .performClick()
        assert(navigateToDeviceInformationIsCalled)
    }

    @Test
    fun aboutScreenNavigateToLicenses() {
        var navigateToLicensesIsCalled = false
        setContentWithTheme {
            AboutScreen(
                animatedContentScope = it,
                openDrawer = {},
                navigateToDeviceInformation = { assert(false) },
                navigateToLicenses = { navigateToLicensesIsCalled = true },
            )
        }
        composeTestRule.onNodeWithText(stringResource(R.string.about_license_title))
            .assertHasClickAction()
            .performClick()
        assert(navigateToLicensesIsCalled)
    }

    @Test
    fun deviceInformationSmokeTest() {
        setContentWithTheme {
            DeviceInformationScreen({}, it)
        }
    }

    @Test
    fun licensesSmokeTest() {
        setContentWithTheme {
            LicensesScreen(it) {}
        }
    }

    @Test
    fun testAboutScreenDrawerOpen() {
        var drawerOpened = false
        setContentWithTheme {
            AboutScreen(
                animatedContentScope = it,
                openDrawer = { drawerOpened = true },
                navigateToDeviceInformation = {},
                navigateToLicenses = {},
            )
        }
        composeTestRule.onNodeWithContentDescription("Open Drawer")
            .assertHasClickAction()
            .performClick()
        assert(drawerOpened)
    }

    @Test
    fun testDeviceInformationContentVisible() {
        setContentWithTheme {
            DeviceInformationScreen({}, it)
        }
        composeTestRule.onNodeWithText(stringResource(R.string.device_information))
            .assertHasClickAction()
    }

    @Test
    fun testLicensesContentVisible() {
        setContentWithTheme {
            LicensesScreen(it) {}
        }
        composeTestRule.onNodeWithText(stringResource(R.string.about_license_title))
            .assertHasClickAction()
    }

    @Test
    fun testScrollThroughDeviceInformation() {
        setContentWithTheme {
            DeviceInformationScreen({}, it)
        }
        composeTestRule.onNodeWithText(stringResource(R.string.device_information))
            .performScrollTo()
    }

    @Test
    fun testScrollThroughLicenses() {
        setContentWithTheme {
            LicensesScreen(it) {}
        }
        composeTestRule.onNodeWithText(stringResource(R.string.about_license_title))
            .performScrollTo()
    }

    @Test
    fun testAboutScreenMultipleClicks() {
        var deviceInfoClicked = false
        var licensesClicked = false
        setContentWithTheme {
            AboutScreen(
                animatedContentScope = it,
                openDrawer = {},
                navigateToDeviceInformation = { deviceInfoClicked = true },
                navigateToLicenses = { licensesClicked = true },
            )
        }
        composeTestRule.onNodeWithContentDescription(stringResource(R.string.device_information))
            .performClick()
        composeTestRule.onNodeWithText(stringResource(R.string.about_license_title))
            .performClick()
        assert(deviceInfoClicked && licensesClicked)
    }
}
 
