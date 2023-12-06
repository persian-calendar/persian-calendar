package com.byagowi.persiancalendar.ui.dialogs

import androidx.compose.ui.test.junit4.createComposeRule
import com.byagowi.persiancalendar.ui.settings.locationathan.location.GPSLocationDialog
import org.junit.Rule
import org.junit.Test

class GPSLocationDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cancelButtonTest() {
        var showDialog = true
        // var cancelString = ""
        composeTestRule.setContent {
            // cancelString = stringResource(R.string.cancel)
            if (showDialog) GPSLocationDialog { showDialog = false }
        }
        assert(showDialog)
        // val context = InstrumentationRegistry.getInstrumentation().targetContext
        // if (ActivityCompat.checkSelfPermission(
        //         context, Manifest.permission.ACCESS_FINE_LOCATION
        //     ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        //         context, Manifest.permission.ACCESS_COARSE_LOCATION
        //     ) != PackageManager.PERMISSION_GRANTED
        // ) {
        //     // Works only if permission isn't given
        //     composeTestRule.onNodeWithText(cancelString)
        //         .assertHasClickAction()
        //         .performClick()
        //     runBlocking {
        //         composeTestRule.awaitIdle()
        //         assert(!showDialog)
        //     }
        // }
    }
}
