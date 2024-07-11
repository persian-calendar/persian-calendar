package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_INFO
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_LICENSES
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Have a look at https://developer.android.com/static/images/jetpack/compose/compose-testing-cheatsheet.pdf
@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class AboutScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aboutScreenNavigateToDeviceInformation() {
        var navigateToDeviceInformationIsCalled = false
        var deviceInformationString = ""
        composeTestRule.setContentWithParent { scope ->
            deviceInformationString = stringResource(R.string.device_information)
            Box(
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState(key = SHARED_CONTENT_KEY_INFO),
                    animatedVisibilityScope = scope,
                ),
            ) {
                AppIconButton(
                    icon = Icons.Default.PermDeviceInformation,
                    title = stringResource(R.string.device_information),
                    onClick = { navigateToDeviceInformationIsCalled = true },
                )
            }
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
        composeTestRule.setContentWithParent { scope ->
            licensesString = stringResource(R.string.about_license_title)
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        title = { Text(stringResource(R.string.about)) },
                        colors = appTopAppBarColors(),
                        navigationIcon = { NavigationOpenDrawerIcon {} },
                        actions = {
                            Text(
                                licensesString,
                                modifier = Modifier
                                    .sharedBounds(
                                        rememberSharedContentState(key = SHARED_CONTENT_KEY_LICENSES),
                                        animatedVisibilityScope = scope,
                                    )
                                    .clickable { navigateToLicensesIsCalled = true }
                            )
                        }
                    )
                }
            ) { paddingValues -> paddingValues.let {} }
        }
        composeTestRule.onNodeWithText(licensesString)
            .assertHasClickAction()
            .performClick()
        assert(navigateToLicensesIsCalled)
    }

    @Test
    fun deviceInformationSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            Box(
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState(key = SHARED_CONTENT_KEY_INFO),
                    animatedVisibilityScope = scope,
                ),
            ) {
                AppIconButton(
                    icon = Icons.Default.PermDeviceInformation,
                    title = stringResource(R.string.device_information),
                    onClick = {},
                )
            }
        }
    }

    @Test
    fun licensesSmokeTest() {
        composeTestRule.setContentWithParent { scope ->
            Box(
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState(key = SHARED_CONTENT_KEY_LICENSES),
                    animatedVisibilityScope = scope,
                ),
            ) {
                AppIconButton(
                    icon = Icons.Default.PermDeviceInformation,
                    title = stringResource(R.string.licenses),
                    onClick = {},
                )
            }
        }
    }
}
