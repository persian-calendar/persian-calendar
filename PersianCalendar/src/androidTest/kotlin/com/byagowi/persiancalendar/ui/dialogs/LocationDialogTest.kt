package com.byagowi.persiancalendar.ui.dialogs

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.settings.locationathan.location.LocationDialog
import org.junit.Rule
import org.junit.Test

class LocationDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        var locationString = ""
        composeTestRule.setContent {
            locationString = stringResource(R.string.location)
            LocationDialog({}, {})
        }
        composeTestRule.onNodeWithText(locationString)
    }
}
