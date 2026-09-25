package com.byagowi.persiancalendar.ui.dialogs

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.location
import com.byagowi.persiancalendar.ui.settings.locationathan.location.LocationDialog
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test

class LocationDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        var locationString = ""
        composeTestRule.setContent {
            locationString = stringResource(Res.string.location)
            LocationDialog {}
        }
        composeTestRule.onNodeWithText(locationString).assertExists()
    }
}
