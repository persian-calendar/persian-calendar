package com.byagowi.persiancalendar.ui.dialogs

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.about.EmailDialog
import org.junit.Rule
import org.junit.Test

class EmailDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cancelButtonTest() {
        var showDialog = true
        var cancelString = ""
        composeTestRule.setContent {
            cancelString = stringResource(R.string.cancel)
            if (showDialog) EmailDialog { showDialog = false }
        }
        assert(showDialog)
        composeTestRule.onNodeWithText(cancelString)
            .assertHasClickAction()
            .performClick()
        assert(!showDialog)
    }
}
