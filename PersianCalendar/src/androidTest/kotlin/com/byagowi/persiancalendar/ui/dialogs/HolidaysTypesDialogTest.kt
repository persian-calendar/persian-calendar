package com.byagowi.persiancalendar.ui.dialogs

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.HolidaysTypesDialog
import com.byagowi.persiancalendar.ui.settings.locationathan.location.LocationDialog
import org.junit.Rule
import org.junit.Test

class HolidaysTypesDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun basicSmokeTest() {
        var eventsString = ""
        composeTestRule.setContent {
            eventsString = stringResource(R.string.events)
            HolidaysTypesDialog {}
        }
        composeTestRule.onNodeWithText(eventsString)
    }
}
